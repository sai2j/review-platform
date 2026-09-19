package com.nit.appeal;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nit.audit.AuditLogService;
import com.nit.review.Review;
import com.nit.review.ReviewRepository;
import com.nit.user.User;
import com.nit.user.UserRepository;

@Service
public class AppealService {

    private final AppealRepository appealRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AppealService(
            AppealRepository appealRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {

        this.appealRepository = appealRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    // =========================
    // CREATE APPEAL
    // =========================

    public Appeal createAppeal(
            Long reviewId,
            String reason) {

        Review review =
                reviewRepository.findById(reviewId)
                        .orElse(null);

        if (review == null) {
            throw new RuntimeException(
                    "Review not found");
        }

        // =========================
        // REASON VALIDATION
        // =========================

        if (reason == null
                || reason.isBlank()) {

            throw new RuntimeException(
                    "Appeal reason is required");
        }

        reason = reason.trim();

        if (reason.length() > 1000) {

            throw new RuntimeException(
                    "Appeal reason must not exceed 1000 characters");
        }

        User loggedInUser =
                getLoggedInUser();

        // =========================
        // OBJECT-LEVEL AUTHORIZATION
        // =========================

        if (review.getUserId() == null
                || !review.getUserId()
                        .equals(loggedInUser.getId())) {

            throw new AccessDeniedException(
                    "You can only appeal your own review");
        }

        Appeal appeal =
                new Appeal(
                        reviewId,
                        loggedInUser.getId(),
                        reason
                );

        Appeal savedAppeal =
                appealRepository.save(appeal);

        // =========================
        // AUDIT LOG
        // =========================

        auditLogService.log(
                "APPEAL_CREATED",
                "APPEAL",
                savedAppeal.getId(),
                "Appeal created for review "
                        + reviewId
        );

        return savedAppeal;
    }

    // =========================
    // GET APPEALS BY REVIEW
    // =========================

    public List<Appeal> getAppealsByReviewId(
            Long reviewId) {

        Review review =
                reviewRepository.findById(reviewId)
                        .orElse(null);

        if (review == null) {
            throw new RuntimeException(
                    "Review not found");
        }

        User loggedInUser =
                getLoggedInUser();

        // ADMIN can view all appeals for the review
        if ("ADMIN".equalsIgnoreCase(
                loggedInUser.getRole())) {

            return appealRepository
                    .findByReviewId(reviewId);
        }

        // Review owner can only see their own appeals
        if (review.getUserId() == null
                || !review.getUserId()
                        .equals(loggedInUser.getId())) {

            throw new AccessDeniedException(
                    "You are not authorized to view these appeals");
        }

        return appealRepository
                .findByReviewId(reviewId)
                .stream()
                .filter(appeal ->
                        appeal.getUserId() != null
                                && appeal.getUserId()
                                        .equals(loggedInUser.getId()))
                .collect(Collectors.toList());
    }

    // =========================
    // GET ALL APPEALS
    // =========================

    public List<Appeal> getAllAppeals() {

        return appealRepository.findAll();
    }

    // =========================
    // GET APPEAL BY ID
    // =========================

    public Appeal getAppealById(
            Long id) {

        return appealRepository
                .findById(id)
                .orElse(null);
    }

    // =========================
    // UPDATE APPEAL STATUS
    // =========================

    public Appeal updateAppealStatus(
            Long id,
            String status,
            String decision) {

        Appeal appeal =
                appealRepository
                        .findById(id)
                        .orElse(null);

        if (appeal == null) {
            throw new RuntimeException(
                    "Appeal not found");
        }

        // =========================
        // STATUS VALIDATION
        // =========================

        if (status == null
                || status.isBlank()) {

            throw new RuntimeException(
                    "Appeal status is required");
        }

        status = status.trim().toUpperCase();

        if (!status.equals("PENDING")
                && !status.equals("APPROVED")
                && !status.equals("REJECTED")) {

            throw new RuntimeException(
                    "Invalid appeal status");
        }

        if (decision != null) {
            decision = decision.trim();

            if (decision.length() > 1000) {

                throw new RuntimeException(
                        "Appeal decision must not exceed 1000 characters");
            }
        }

        String oldStatus =
                appeal.getStatus();

        appeal.setStatus(status);
        appeal.setDecision(decision);

        Appeal savedAppeal =
                appealRepository.save(appeal);

        // =========================
        // AUDIT LOG
        // =========================

        auditLogService.log(
                "APPEAL_STATUS_CHANGED",
                "APPEAL",
                savedAppeal.getId(),
                "Appeal status changed from "
                        + oldStatus
                        + " to "
                        + status
        );

        return savedAppeal;
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
                        authentication.getName());

        if (user == null) {

            throw new AccessDeniedException(
                    "User not found");
        }

        return user;
    }
}