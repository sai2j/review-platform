package com.nit.evidence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nit.audit.AuditLogService;
import com.nit.review.Review;
import com.nit.review.ReviewRepository;
import com.nit.user.User;
import com.nit.user.UserRepository;

@Service
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    private final Path evidenceStorage =
            Paths.get("C:\\review-platform-private-evidence")
                    .toAbsolutePath()
                    .normalize();

    // =========================
    // FILE UPLOAD LIMITS
    // =========================

    private static final long MAX_FILE_SIZE =
            10 * 1024 * 1024; // 10 MB

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "application/pdf"
            );

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    ".jpg",
                    ".jpeg",
                    ".png",
                    ".pdf"
            );

    public EvidenceService(
            EvidenceRepository evidenceRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {

        this.evidenceRepository = evidenceRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    // =========================
    // MODERATOR / ADMIN
    // REQUEST EVIDENCE
    // =========================

    public String requestEvidence(Long reviewId) {

        Review review =
                reviewRepository.findById(reviewId).orElse(null);

        if (review == null) {
            throw new RuntimeException(
                    "Review not found");
        }

        User moderator =
                getLoggedInUser();

        // =========================
        // ROLE CHECK
        // =========================

        String role =
                moderator.getRole();

        if (!"ADMIN".equalsIgnoreCase(role)
                && !"MODERATOR".equalsIgnoreCase(role)) {

            throw new AccessDeniedException(
                    "You are not authorized to request evidence");
        }

        // =========================
        // MARK REVIEW UNDER REVIEW
        // =========================

        review.setVerificationStatus(
                "UNDER_REVIEW"
        );

        reviewRepository.save(review);

        // =========================
        // AUDIT LOG
        // =========================

        auditLogService.log(
                "EVIDENCE_REQUESTED",
                "REVIEW",
                reviewId,
                "Supporting evidence requested by moderator/admin"
        );

        return "Supporting evidence has been requested for review "
                + reviewId;
    }

    // =========================
    // UPLOAD EVIDENCE
    // =========================

    public Evidence uploadEvidence(
            Long reviewId,
            MultipartFile file) {

        // =========================
        // BASIC FILE VALIDATION
        // =========================

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Evidence file is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException(
                    "Evidence file size must not exceed 10 MB");
        }

        Review review =
                reviewRepository.findById(reviewId).orElse(null);

        if (review == null) {
            throw new RuntimeException(
                    "Review not found");
        }

        User loggedInUser =
                getLoggedInUser();

        // =========================
        // ACCESS CONTROL
        // =========================

        checkReviewAccess(
                review,
                loggedInUser
        );

        try {

            Files.createDirectories(
                    evidenceStorage
            );

            // =========================
            // ORIGINAL FILE NAME
            // =========================

            String originalFileName =
                    file.getOriginalFilename();

            if (originalFileName == null
                    || originalFileName.isBlank()) {

                throw new RuntimeException(
                        "Invalid evidence file name");
            }

            // Remove any path information
            String safeFileName =
                    Paths.get(originalFileName)
                            .getFileName()
                            .toString();

            // =========================
            // FILE EXTENSION VALIDATION
            // =========================

            String lowerFileName =
                    safeFileName.toLowerCase();

            String extension = "";

            int lastDot =
                    lowerFileName.lastIndexOf(".");

            if (lastDot >= 0) {
                extension =
                        lowerFileName.substring(lastDot);
            }

            if (!ALLOWED_EXTENSIONS.contains(extension)) {

                throw new RuntimeException(
                        "File type is not allowed");
            }

            // =========================
            // CONTENT TYPE VALIDATION
            // =========================

            String contentType =
                    file.getContentType();

            if (contentType == null
                    || !ALLOWED_CONTENT_TYPES
                            .contains(contentType.toLowerCase())) {

                throw new RuntimeException(
                        "Unsupported file content type");
            }

            // =========================
            // EXTENSION + CONTENT TYPE
            // MUST MATCH
            // =========================

            boolean validFileType = false;

            if ((".jpg".equals(extension)
                    || ".jpeg".equals(extension))
                    && "image/jpeg".equalsIgnoreCase(
                            contentType)) {

                validFileType = true;
            }

            if (".png".equals(extension)
                    && "image/png".equalsIgnoreCase(
                            contentType)) {

                validFileType = true;
            }

            if (".pdf".equals(extension)
                    && "application/pdf"
                            .equalsIgnoreCase(contentType)) {

                validFileType = true;
            }

            if (!validFileType) {

                throw new RuntimeException(
                        "File extension and content type do not match");
            }

            // =========================
            // RANDOM STORED FILE NAME
            // =========================

            String storedFileName =
                    UUID.randomUUID()
                            .toString()
                            + extension;

            // =========================
            // SAFE STORAGE PATH
            // =========================

            Path targetPath =
                    evidenceStorage
                            .resolve(storedFileName)
                            .normalize();

            if (!targetPath.startsWith(
                    evidenceStorage)) {

                throw new RuntimeException(
                        "Invalid evidence file path");
            }

            // =========================
            // STORE FILE
            // =========================

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            Evidence evidence =
                    new Evidence(
                            reviewId,
                            safeFileName,
                            contentType,
                            targetPath.toString(),
                            loggedInUser.getId()
                    );

            Evidence savedEvidence =
                    evidenceRepository.save(
                            evidence
                    );

            // =========================
            // AUDIT LOG
            // =========================

            auditLogService.log(
                    "EVIDENCE_UPLOADED",
                    "EVIDENCE",
                    savedEvidence.getId(),
                    "Evidence uploaded for review "
                            + reviewId
            );

            return savedEvidence;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to store evidence file"
            );
        }
    }

    // =========================
    // GET EVIDENCE BY REVIEW
    // =========================

    public List<Evidence> getEvidenceByReviewId(
            Long reviewId) {

        Review review =
                reviewRepository
                        .findById(reviewId)
                        .orElse(null);

        if (review == null) {
            throw new RuntimeException(
                    "Review not found");
        }

        User loggedInUser =
                getLoggedInUser();

        // Only review owner or ADMIN can access evidence list
        checkReviewAccess(
                review,
                loggedInUser
        );

        return evidenceRepository
                .findByReviewId(reviewId);
    }

    // =========================
    // GET EVIDENCE BY ID
    // =========================

    public Evidence getEvidenceById(Long id) {

        Evidence evidence =
                evidenceRepository
                        .findById(id)
                        .orElse(null);

        if (evidence == null) {
            throw new RuntimeException(
                    "Evidence not found");
        }

        User loggedInUser =
                getLoggedInUser();

        Review review =
                reviewRepository
                        .findById(
                                evidence.getReviewId()
                        )
                        .orElse(null);

        if (review == null) {
            throw new RuntimeException(
                    "Review not found");
        }

        // Only review owner or ADMIN can access evidence
        checkReviewAccess(
                review,
                loggedInUser
        );

        return evidence;
    }

    // =========================
    // GET EVIDENCE FILE
    // =========================

    public Path getEvidenceFile(Long id) {

        Evidence evidence =
                evidenceRepository
                        .findById(id)
                        .orElse(null);

        if (evidence == null) {
            throw new RuntimeException(
                    "Evidence not found");
        }

        User loggedInUser =
                getLoggedInUser();

        Review review =
                reviewRepository
                        .findById(
                                evidence.getReviewId()
                        )
                        .orElse(null);

        if (review == null) {
            throw new RuntimeException(
                    "Review not found");
        }

        // Only review owner or ADMIN can download/view evidence
        checkReviewAccess(
                review,
                loggedInUser
        );

        Path path =
                Paths.get(
                        evidence.getStoragePath()
                )
                .toAbsolutePath()
                .normalize();

        // Make sure file is still inside private evidence storage
        if (!path.startsWith(
                evidenceStorage)) {

            throw new AccessDeniedException(
                    "Invalid evidence storage path");
        }

        if (!Files.exists(path)) {

            throw new RuntimeException(
                    "Evidence file not found");
        }

        return path;
    }

    // =========================
    // REVIEW ACCESS CHECK
    // =========================

    private void checkReviewAccess(
            Review review,
            User loggedInUser) {

        if (loggedInUser == null) {

            throw new AccessDeniedException(
                    "You must be logged in");
        }

        // ADMIN has access
        if ("ADMIN".equalsIgnoreCase(
                loggedInUser.getRole())) {

            return;
        }

        // Review owner has access
        if (review.getUserId() != null
                && review.getUserId()
                        .equals(loggedInUser.getId())) {

            return;
        }

        throw new AccessDeniedException(
                "You are not authorized to access this evidence");
    }

    // =========================
    // LOGGED-IN USER
    // =========================

    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "You must be logged in");
        }

        User user =
                userRepository.findByEmail(
                        authentication.getName()
                );

        if (user == null) {

            throw new AccessDeniedException(
                    "User not found");
        }

        return user;
    }
}