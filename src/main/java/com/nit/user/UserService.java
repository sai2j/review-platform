package com.nit.user;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nit.admin.AdminRepository;
import com.nit.dto.PublicUserProfileDTO;
import com.nit.dto.UserProfileDTO;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AvatarStorageService avatarStorageService;

    public UserService(
            UserRepository userRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            AvatarStorageService avatarStorageService) {

        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.avatarStorageService = avatarStorageService;
    }

    public User registerUser(String email, String password) {
        String normalizedEmail = email == null
                ? ""
                : email.trim().toLowerCase();

        if (normalizedEmail.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));

        // Public registration always creates a normal user.
        user.setRole("USER");
        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }

    /**
     * Creates a normal user.
     * Role and status supplied by the request are not trusted.
     */
    public User saveUser(User user) {
        if (user == null) {
            throw new RuntimeException("User data is required");
        }

        if (user.getEmail() == null
                || user.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        String normalizedEmail =
                user.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new RuntimeException("Email already registered");
        }

        if (user.getPassword() == null
                || user.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        if (user.getPassword().length() < 8
                || user.getPassword().length() > 100) {
            throw new RuntimeException(
                    "Password must be between 8 and 100 characters");
        }

        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Never accept ADMIN or MODERATOR through this generic method.
        user.setRole("USER");
        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByEmail(String email) {
        String normalizedEmail = email == null
                ? ""
                : email.trim().toLowerCase();

        return userRepository.findByEmailIgnoreCase(normalizedEmail);
    }

    public boolean isAdmin(Long userId) {
        return adminRepository.existsByUserId(userId);
    }

    /**
     * Assign or remove the MODERATOR role.
     * Only an authenticated ADMIN can perform this operation.
     *
     * enabled = true  -> MODERATOR
     * enabled = false -> USER
     *
     * An existing ADMIN account cannot be changed through this method.
     */
    @Transactional
    public User updateModeratorRole(Long id, boolean enabled) {
        requireAdmin();

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        String currentRole = user.getRole();

        if (currentRole == null || currentRole.isBlank()) {
            currentRole = "USER";
        } else {
            currentRole = currentRole.trim().toUpperCase();
        }

        if ("ADMIN".equals(currentRole)) {
            throw new RuntimeException(
                    "An ADMIN account cannot be changed through moderator management");
        }

        if (!"USER".equals(currentRole)
                && !"MODERATOR".equals(currentRole)) {
            throw new RuntimeException(
                    "Only USER and MODERATOR roles can be changed here");
        }

        if (enabled) {
            user.setRole("MODERATOR");
        } else {
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    private void requireAdmin() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException(
                    "You must be logged in as an ADMIN");
        }

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority()));

        if (!isAdmin) {
            throw new AccessDeniedException(
                    "Only ADMIN can manage moderator roles");
        }
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void deleteOwnAccount() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("You must be logged in");
        }

        User user = userRepository.findByEmailIgnoreCase(
                authentication.getName());

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Long userId = user.getId();

        user.setEmail(
                "deleted-user-" + userId + "@deleted.local");

        user.setPassword(
                passwordEncoder.encode(UUID.randomUUID().toString()));

        user.setStatus("RESTRICTED");

        userRepository.save(user);
    }

    public User updateUserStatus(Long id, String status) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status is required");
        }

        status = status.trim().toUpperCase();

        if (!status.equals("ACTIVE")
                && !status.equals("RESTRICTED")) {
            throw new RuntimeException(
                    "Status must be ACTIVE or RESTRICTED");
        }

        user.setStatus(status);
        return userRepository.save(user);
    }

    public User updateUserPassword(Long id, String newPassword) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("Password is required");
        }

        if (newPassword.length() < 8
                || newPassword.length() > 100) {
            throw new RuntimeException(
                    "Password must be between 8 and 100 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    // PROFILE: GET MY PROFILE

    @Transactional(readOnly = true)
    public UserProfileDTO getMyProfile() {
        return toUserProfileDTO(getAuthenticatedUser());
    }

    // PROFILE: UPDATE EDITABLE FIELDS

    @Transactional
    public UserProfileDTO updateMyProfile(
            UserProfileDTO profileDTO) {

        if (profileDTO == null) {
            throw new RuntimeException("Profile data is required");
        }

        User user = getAuthenticatedUser();

        if (profileDTO.getName() == null
                || profileDTO.getName().isBlank()) {
            throw new RuntimeException("Display name is required");
        }

        String name = profileDTO.getName().trim();

        if (name.length() > 255) {
            throw new RuntimeException(
                    "Display name cannot exceed 255 characters");
        }

        if (profileDTO.getBio() != null
                && profileDTO.getBio().length() > 500) {
            throw new RuntimeException(
                    "Bio cannot exceed 500 characters");
        }

        if (profileDTO.getCountry() != null
                && profileDTO.getCountry().length() > 100) {
            throw new RuntimeException(
                    "Country cannot exceed 100 characters");
        }

        user.setName(name);
        user.setBio(normalizeOptionalText(profileDTO.getBio()));
        user.setCountry(normalizeOptionalText(profileDTO.getCountry()));

        if (profileDTO.getProfilePublic() != null) {
            user.setProfilePublic(profileDTO.getProfilePublic());
        } else if (user.getProfilePublic() == null) {
            user.setProfilePublic(true);
        }

        // Never accept an arbitrary avatar URL from the request body.

        User savedUser = userRepository.save(user);

        return toUserProfileDTO(savedUser);
    }

    // PROFILE: UPLOAD OR REPLACE AVATAR

    @Transactional
    public UserProfileDTO uploadMyAvatar(MultipartFile file) {
        User user = getAuthenticatedUser();

        String avatarUrl = avatarStorageService.store(file);

        user.setAvatarUrl(avatarUrl);

        User savedUser = userRepository.save(user);

        return toUserProfileDTO(savedUser);
    }

    // PUBLIC PROFILE

    @Transactional(readOnly = true)
    public PublicUserProfileDTO getPublicProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("Profile not found");
        }

        if (!Boolean.TRUE.equals(user.getProfilePublic())) {
            throw new AccessDeniedException(
                    "This profile is private");
        }

        return new PublicUserProfileDTO(
                user.getId(),
                user.getName(),
                user.getBio(),
                user.getCountry(),
                user.getAvatarUrl());
    }

    private User getAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {
            throw new AccessDeniedException("Please login first");
        }

        User user = userRepository.findByEmailIgnoreCase(
                authentication.getName());

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AccessDeniedException(
                    "Your account is not active");
        }

        return user;
    }

    private UserProfileDTO toUserProfileDTO(User user) {
        return new UserProfileDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBio(),
                user.getCountry(),
                user.getProfilePublic(),
                user.getAvatarUrl());
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}