package com.nit.evidence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
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

    public Evidence uploadEvidence(
            Long reviewId,
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Evidence file is required");
        }

        Review review =
                reviewRepository.findById(reviewId).orElse(null);

        if (review == null) {
            throw new RuntimeException("Review not found");
        }

        User loggedInUser = getLoggedInUser();

        // Only the review owner or ADMIN can upload evidence
        checkReviewAccess(review, loggedInUser);

        try {

            Files.createDirectories(evidenceStorage);

            String originalFileName =
                    file.getOriginalFilename();

            if (originalFileName == null
                    || originalFileName.isBlank()) {

                throw new RuntimeException(
                        "Invalid evidence file name");
            }

            String safeFileName =
                    Paths.get(originalFileName)
                            .getFileName()
                            .toString();

            String storedFileName =
                    UUID.randomUUID().toString()
                    + "_"
                    + safeFileName;

            Path targetPath =
                    evidenceStorage
                            .resolve(storedFileName)
                            .normalize();

            if (!targetPath.startsWith(evidenceStorage)) {
                throw new RuntimeException(
                        "Invalid evidence file path");
            }

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            Evidence evidence = new Evidence(
                    reviewId,
                    safeFileName,
                    file.getContentType(),
                    targetPath.toString(),
                    loggedInUser.getId()
            );

            Evidence savedEvidence =
                    evidenceRepository.save(evidence);

            auditLogService.log(
                    "EVIDENCE_UPLOADED",
                    "EVIDENCE",
                    savedEvidence.getId(),
                    "Evidence uploaded for review " + reviewId
            );

            return savedEvidence;

        } catch (IOException e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Unable to store evidence file: " + e.getMessage());
        }
    }

    public List<Evidence> getEvidenceByReviewId(Long reviewId) {

        Review review =
                reviewRepository.findById(reviewId).orElse(null);

        if (review == null) {
            throw new RuntimeException("Review not found");
        }

        User loggedInUser = getLoggedInUser();

        // Only review owner or ADMIN can access evidence list
        checkReviewAccess(review, loggedInUser);

        return evidenceRepository.findByReviewId(reviewId);
    }

    public Evidence getEvidenceById(Long id) {

        Evidence evidence =
                evidenceRepository
                        .findById(id)
                        .orElse(null);

        if (evidence == null) {
            throw new RuntimeException("Evidence not found");
        }

        User loggedInUser = getLoggedInUser();

        Review review =
                reviewRepository
                        .findById(evidence.getReviewId())
                        .orElse(null);

        if (review == null) {
            throw new RuntimeException("Review not found");
        }

        // Only review owner or ADMIN can access evidence
        checkReviewAccess(review, loggedInUser);

        return evidence;
    }

    public Path getEvidenceFile(Long id) {

        Evidence evidence =
                evidenceRepository
                        .findById(id)
                        .orElse(null);

        if (evidence == null) {
            throw new RuntimeException("Evidence not found");
        }

        User loggedInUser = getLoggedInUser();

        Review review =
                reviewRepository
                        .findById(evidence.getReviewId())
                        .orElse(null);

        if (review == null) {
            throw new RuntimeException("Review not found");
        }

        // Only review owner or ADMIN can download/view evidence
        checkReviewAccess(review, loggedInUser);

        Path path =
                Paths.get(evidence.getStoragePath())
                        .toAbsolutePath()
                        .normalize();

        // Make sure file is still inside private evidence storage
        if (!path.startsWith(evidenceStorage)) {
            throw new AccessDeniedException(
                    "Invalid evidence storage path");
        }

        if (!Files.exists(path)) {
            throw new RuntimeException(
                    "Evidence file not found");
        }

        return path;
    }

    private void checkReviewAccess(
            Review review,
            User loggedInUser) {

        if (loggedInUser == null) {
            throw new AccessDeniedException(
                    "You must be logged in");
        }

        // ADMIN has access
        if ("ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            return;
        }

        // Review owner has access
        if (review.getUserId() != null
                && review.getUserId().equals(loggedInUser.getId())) {
            return;
        }

        throw new AccessDeniedException(
                "You are not authorized to access this evidence");
    }

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
                        authentication.getName());

        if (user == null) {
            throw new AccessDeniedException(
                    "User not found");
        }

        return user;
    }
}