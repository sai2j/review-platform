package com.nit.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import jakarta.validation.Valid;

import com.nit.security.BruteForceProtectionService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final BruteForceProtectionService
            bruteForceProtectionService;

    public UserController(
            UserService userService,
            AuthenticationManager authenticationManager,
            BruteForceProtectionService
                    bruteForceProtectionService) {

        this.userService = userService;

        this.authenticationManager =
                authenticationManager;

        this.bruteForceProtectionService =
                bruteForceProtectionService;
    }

    // =========================
    // REGISTER USER
    // =========================

    @PostMapping("/register")
    public User registerUser(
            @Valid @RequestBody User user) {

        return userService.registerUser(
                user.getEmail(),
                user.getPassword()
        );
    }

    // =========================
    // LOGIN USER
    // =========================

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(
            @RequestBody User user,
            HttpServletRequest request,
            HttpServletResponse response) {

        String email =
                user.getEmail() == null
                        ? ""
                        : user.getEmail()
                                .trim()
                                .toLowerCase();

        String clientIp =
                request.getRemoteAddr();

        String protectionKey =
                clientIp + ":" + email;

        // =========================
        // CHECK BRUTE-FORCE BLOCK
        // =========================

        if (bruteForceProtectionService
                .isBlocked(protectionKey)) {

            Map<String, Object> errorResponse =
                    new HashMap<>();

            errorResponse.put(
                    "error",
                    "Too many failed login attempts. Please try again later."
            );

            return ResponseEntity
                    .status(
                            HttpStatus.TOO_MANY_REQUESTS
                    )
                    .body(errorResponse);
        }

        try {

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    user.getEmail(),
                                    user.getPassword()
                            )
                    );

            // =========================
            // CLEAR FAILED ATTEMPTS
            // =========================

            bruteForceProtectionService
                    .recordSuccessfulLogin(
                            protectionKey
                    );

            // =========================
            // CREATE SECURITY CONTEXT
            // =========================

            SecurityContext securityContext =
                    SecurityContextHolder
                            .createEmptyContext();

            securityContext.setAuthentication(
                    authentication
            );

            SecurityContextHolder.setContext(
                    securityContext
            );

            // =========================
            // CREATE HTTP SESSION
            // =========================

            request.getSession(true);

            // =========================
            // SAVE LOGIN IN SESSION
            // =========================

            HttpSessionSecurityContextRepository
                    securityContextRepository =
                    new HttpSessionSecurityContextRepository();

            securityContextRepository.saveContext(
                    securityContext,
                    request,
                    response
            );

            // =========================
            // GET LOGGED-IN USER
            // =========================

            User loggedInUser =
                    userService.getUserByEmail(
                            user.getEmail()
                    );

            // =========================
            // CHECK ADMIN
            // =========================

            boolean admin =
                    userService.isAdmin(
                            loggedInUser.getId()
                    );

            // =========================
            // RESPONSE
            // =========================

            Map<String, Object> result =
                    new HashMap<>();

            result.put(
                    "user",
                    loggedInUser
            );

            result.put(
                    "admin",
                    admin
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            bruteForceProtectionService
                    .recordFailedAttempt(
                            protectionKey
                    );

            Map<String, Object> errorResponse =
                    new HashMap<>();

            errorResponse.put(
                    "error",
                    "Invalid email or password"
            );

            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .body(errorResponse);
        }
    }

    // =========================
    // PRIVACY - DELETE MY ACCOUNT
    // =========================

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>>
    deleteMyAccount(
            HttpServletRequest request,
            HttpServletResponse response) {

        userService.deleteOwnAccount();

        // Clear current security session
        SecurityContextHolder.clearContext();

        var session =
                request.getSession(false);

        if (session != null) {

            session.invalidate();
        }

        Map<String, String> result =
                new HashMap<>();

        result.put(
                "message",
                "Your account has been deleted."
        );

        return ResponseEntity.ok(result);
    }

    // =========================
    // CREATE USER
    // ADMIN ONLY
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public User createUser(
            @Valid @RequestBody User user) {

        return userService.saveUser(user);
    }

    // =========================
    // GET ALL USERS
    // ADMIN ONLY
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<User> getAllUsers() {

        return userService.getAllUsers();
    }

    // =========================
    // GET USER BY ID
    // ADMIN ONLY
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public User getUserById(
            @PathVariable Long id) {

        return userService.getUserById(id);
    }

    // =========================
    // ACCOUNT RESTRICTION
    // ADMIN ONLY
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public User updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return userService.updateUserStatus(
                id,
                status
        );
    }

    // =========================
    // DELETE USER
    // ADMIN ONLY
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return "User deleted successfully";
    }
}