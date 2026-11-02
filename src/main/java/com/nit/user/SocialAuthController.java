package com.nit.user;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nit.dto.UserResponseDTO;
import com.nit.security.CustomUserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * One "Log in or sign up" page for everyone:
 *
 *   GET  /users/auth/config        which social buttons are switched on
 *   POST /users/auth/check-email   does an account with this email exist?
 *   POST /users/auth/google        sign in with a Google ID token
 *   POST /users/auth/facebook      sign in with a Facebook access token
 *
 * Email + password still uses the existing /users/register and /users/login.
 * Google and Facebook tokens are always verified on the server.
 */
@RestController
@RequestMapping("/users/auth")
public class SocialAuthController {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${app.google.client-id:}")
    private String googleClientId;

    @Value("${app.facebook.app-id:}")
    private String facebookAppId;

    @Value("${app.facebook.app-secret:}")
    private String facebookAppSecret;

    private final UserService userService;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;

    public SocialAuthController(
            UserService userService,
            UserRepository userRepository,
            CustomUserDetailsService userDetailsService) {

        this.userService = userService;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    // WHICH BUTTONS TO SHOW (these two ids are public by design)

    @GetMapping("/config")
    public Map<String, Object> config() {

        Map<String, Object> result = new HashMap<>();

        result.put("googleClientId",
                googleClientId == null ? "" : googleClientId.trim());

        boolean facebookReady = !isBlank(facebookAppId)
                && !isBlank(facebookAppSecret);

        result.put("facebookAppId",
                facebookReady ? facebookAppId.trim() : "");

        return result;
    }

    // EMAIL STEP: LOG IN OR SIGN UP?

    @PostMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(
            @RequestBody Map<String, String> body) {

        String email = normalizeEmail(body == null ? null : body.get("email"));

        if (email.isEmpty()
                || email.length() > 255
                || !email.contains("@")
                || email.startsWith("@")
                || email.endsWith("@")) {

            return error(HttpStatus.BAD_REQUEST,
                    "Please enter a valid email address.");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("exists", userRepository.existsByEmailIgnoreCase(email));

        return ResponseEntity.ok(result);
    }

    // CONTINUE WITH GOOGLE

    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> google(
            @RequestBody Map<String, String> body,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (isBlank(googleClientId)) {
            return error(HttpStatus.SERVICE_UNAVAILABLE,
                    "Google login is not set up yet.");
        }

        String idToken = body == null ? null : body.get("credential");

        if (isBlank(idToken)) {
            return error(HttpStatus.BAD_REQUEST,
                    "Google sign-in did not return a token.");
        }

        // Google checks the signature and the expiry for us.
        JsonNode claims = getJson(
                "https://oauth2.googleapis.com/tokeninfo?id_token="
                        + encode(idToken));

        if (claims == null) {
            return error(HttpStatus.UNAUTHORIZED,
                    "Google sign-in could not be verified.");
        }

        String audience = text(claims, "aud");
        String issuer = text(claims, "iss");
        String email = text(claims, "email");
        boolean emailVerified = "true".equals(text(claims, "email_verified"));

        boolean issuerOk = "accounts.google.com".equals(issuer)
                || "https://accounts.google.com".equals(issuer);

        if (!googleClientId.trim().equals(audience) || !issuerOk) {
            return error(HttpStatus.UNAUTHORIZED,
                    "Google sign-in could not be verified.");
        }

        if (isBlank(email) || !emailVerified) {
            return error(HttpStatus.UNAUTHORIZED,
                    "Your Google email address is not verified.");
        }

        return signIn(email, text(claims, "name"), request, response);
    }

    // CONTINUE WITH FACEBOOK

    @PostMapping("/facebook")
    public ResponseEntity<Map<String, Object>> facebook(
            @RequestBody Map<String, String> body,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (isBlank(facebookAppId) || isBlank(facebookAppSecret)) {
            return error(HttpStatus.SERVICE_UNAVAILABLE,
                    "Facebook login is not set up yet.");
        }

        String accessToken = body == null ? null : body.get("accessToken");

        if (isBlank(accessToken)) {
            return error(HttpStatus.BAD_REQUEST,
                    "Facebook sign-in did not return a token.");
        }

        String appId = facebookAppId.trim();
        String appToken = appId + "|" + facebookAppSecret.trim();

        // 1) Is this token valid, and was it issued for OUR app?
        JsonNode debug = getJson(
                "https://graph.facebook.com/debug_token?input_token="
                        + encode(accessToken)
                        + "&access_token=" + encode(appToken));

        JsonNode data = debug == null ? null : debug.path("data");

        if (data == null
                || !data.path("is_valid").asBoolean(false)
                || !appId.equals(data.path("app_id").asText(""))) {

            return error(HttpStatus.UNAUTHORIZED,
                    "Facebook sign-in could not be verified.");
        }

        // 2) Who is it?
        JsonNode me = getJson(
                "https://graph.facebook.com/me?fields=id,name,email&access_token="
                        + encode(accessToken));

        String email = me == null ? "" : text(me, "email");

        if (isBlank(email)) {
            return error(HttpStatus.BAD_REQUEST,
                    "Your Facebook account did not share an email address. "
                            + "Please continue with email instead.");
        }

        return signIn(email, text(me, "name"), request, response);
    }

    // SHARED: FIND OR CREATE THE USER, THEN START THE SESSION

    private ResponseEntity<Map<String, Object>> signIn(
            String rawEmail,
            String rawName,
            HttpServletRequest request,
            HttpServletResponse response) {

        String email = normalizeEmail(rawEmail);

        User user = userRepository.findByEmailIgnoreCase(email);

        if (user == null) {

            try {
                // Social users never see this password. It only exists
                // because every account row needs one.
                user = userService.registerUser(
                        email,
                        randomPassword(),
                        cleanName(rawName));

            } catch (RuntimeException e) {
                // Two requests at once: somebody else created it first.
                user = userRepository.findByEmailIgnoreCase(email);
            }
        }

        if (user == null) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not create your account. Please try again.");
        }

        UserDetails details;

        try {
            details = userDetailsService.loadUserByUsername(user.getEmail());
        } catch (UsernameNotFoundException e) {
            return error(HttpStatus.FORBIDDEN,
                    "This account is restricted.");
        }

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        details, null, details.getAuthorities());

        // Same session steps as /users/login
        request.getSession(true);
        request.changeSessionId();

        SecurityContext securityContext =
                SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        new HttpSessionSecurityContextRepository()
                .saveContext(securityContext, request, response);

        Map<String, Object> result = new HashMap<>();

        result.put("user", new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()));

        result.put("admin", userService.isAdmin(user.getId()));

        return ResponseEntity.ok(result);
    }

    // SMALL HELPERS

    private JsonNode getJson(String url) {

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> httpResponse = HTTP.send(
                    httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                return null;
            }

            return MAPPER.readTree(httpResponse.body());

        } catch (IOException e) {
            return null;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private static String text(JsonNode node, String field) {
        return node == null ? "" : node.path(field).asText("");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static String cleanName(String name) {

        if (isBlank(name)) {
            return null;
        }

        String trimmed = name.trim();

        return trimmed.length() > 255 ? trimmed.substring(0, 255) : trimmed;
    }

    /** 48 characters: long enough, and under BCrypt's 72 byte limit. */
    private static String randomPassword() {

        String first = UUID.randomUUID().toString().replace("-", "");
        String second = UUID.randomUUID().toString().replace("-", "");

        return first + second.substring(0, 16);
    }

    private static ResponseEntity<Map<String, Object>> error(
            HttpStatus status,
            String message) {

        Map<String, Object> result = new HashMap<>();
        result.put("error", message);

        return ResponseEntity.status(status).body(result);
    }
}