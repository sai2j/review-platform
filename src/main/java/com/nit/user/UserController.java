
package com.nit.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nit.business.BusinessclaimRepository;
import com.nit.dto.UserResponseDTO;
import com.nit.security.BruteForceProtectionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final BusinessclaimRepository businessClaimRepository;

    public UserController(
            UserService userService,
            AuthenticationManager authenticationManager,
            BruteForceProtectionService bruteForceProtectionService,
            BusinessclaimRepository businessClaimRepository) {

        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.bruteForceProtectionService = bruteForceProtectionService;
        this.businessClaimRepository = businessClaimRepository;
    }

    @PostMapping("/register")
    public UserResponseDTO registerUser(
            @Valid @RequestBody User user) {

        User savedUser = userService.registerUser(
                user.getEmail(),
                user.getPassword()
        );

        return toUserResponseDTO(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(
            @RequestBody User user,
            HttpServletRequest request,
            HttpServletResponse response) {

        String email = user.getEmail() == null
                ? ""
                : user.getEmail().trim().toLowerCase();

        String password = user.getPassword();
        String clientIp = request.getRemoteAddr();
        String protectionKey = clientIp + ":" + email;

        if (bruteForceProtectionService.isBlocked(protectionKey)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(
                    "error",
                    "Too many failed login attempts. Please try again later."
            );

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(errorResponse);
        }

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    password
                            )
                    );

            bruteForceProtectionService.recordSuccessfulLogin(protectionKey);

            request.getSession(true);
            request.changeSessionId();

            SecurityContext securityContext =
                    SecurityContextHolder.createEmptyContext();

            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            HttpSessionSecurityContextRepository securityContextRepository =
                    new HttpSessionSecurityContextRepository();

            securityContextRepository.saveContext(
                    securityContext,
                    request,
                    response
            );

            User loggedInUser = userService.getUserByEmail(email);

            if (loggedInUser == null) {
                SecurityContextHolder.clearContext();

                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }

                throw new RuntimeException("Authenticated user not found");
            }

            boolean admin = userService.isAdmin(loggedInUser.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("user", toUserResponseDTO(loggedInUser));
            result.put("admin", admin);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            bruteForceProtectionService.recordFailedAttempt(protectionKey);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid email or password");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logoutUser(
            HttpServletRequest request) {

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Map<String, String> result = new HashMap<>();
        result.put("message", "Logout successful");

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteMyAccount(
            HttpServletRequest request,
            HttpServletResponse response) {

        userService.deleteOwnAccount();
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Map<String, String> result = new HashMap<>();
        result.put("message", "Your account has been deleted.");

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public UserResponseDTO createUser(
            @Valid @RequestBody User user) {

        return toUserResponseDTO(userService.saveUser(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponseDTO> getAllUsers() {

        return userService.getAllUsers()
                .stream()
                .map(this::toUserResponseDTO)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public UserResponseDTO getUserById(
            @PathVariable Long id) {

        return toUserResponseDTO(userService.getUserById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public UserResponseDTO updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return toUserResponseDTO(
                userService.updateUserStatus(id, status)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/password")
    public UserResponseDTO updateUserPassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {

        return toUserResponseDTO(
                userService.updateUserPassword(id, newPassword)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "User deleted successfully";
    }

    private UserResponseDTO toUserResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }
}