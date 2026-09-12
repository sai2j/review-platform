package com.nit.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public UserController(
            UserService userService,
            AuthenticationManager authenticationManager) {

        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    // =========================
    // REGISTER USER
    // =========================

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {

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

        try {

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    user.getEmail(),
                                    user.getPassword()
                            )
                    );

            // =========================
            // CREATE SECURITY CONTEXT
            // =========================

            SecurityContext securityContext =
                    SecurityContextHolder.createEmptyContext();

            securityContext.setAuthentication(authentication);

            SecurityContextHolder.setContext(securityContext);

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
                    userService.getUserByEmail(user.getEmail());

            // =========================
            // CHECK ADMIN
            // =========================

            boolean admin =
                    userService.isAdmin(loggedInUser.getId());

            // =========================
            // RESPONSE
            // =========================

            Map<String, Object> result =
                    new HashMap<>();

            result.put("user", loggedInUser);
            result.put("admin", admin);

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            Map<String, Object> errorResponse =
                    new HashMap<>();

            errorResponse.put(
                    "error",
                    "Invalid email or password"
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse);
        }
    }

    // =========================
    // CREATE USER
    // =========================

    @PostMapping
    public User createUser(@RequestBody User user) {

        return userService.saveUser(user);
    }

    // =========================
    // GET ALL USERS
    // =========================

    @GetMapping
    public List<User> getAllUsers() {

        return userService.getAllUsers();
    }

    // =========================
    // GET USER BY ID
    // =========================

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {

        return userService.getUserById(id);
    }

    // =========================
    // DELETE USER
    // =========================

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return "User deleted successfully";
    }
}