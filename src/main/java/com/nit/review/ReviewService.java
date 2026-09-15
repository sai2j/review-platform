package com.nit.review;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nit.Report.ReportRepository;
import com.nit.Website.WebsiteRepository;
import com.nit.admin.AdminRepository;
import com.nit.audit.AuditLogService;
import com.nit.business.BusinessResponseRepository;
import com.nit.user.User;
import com.nit.user.UserRepository;

@Service
public class ReviewService {

    public final ReviewRepository reviewRepository;

    private final UserRepository userRepository;

    private final WebsiteRepository websiteRepository;

    private final AdminRepository adminRepository;

    private final ReviewVoteRepository reviewVoteRepository;

    private final ReportRepository reportRepository;

    private final BusinessResponseRepository businessResponseRepository;

    private final AuditLogService auditLogService;

    public ReviewService(

            ReviewRepository reviewRepository,

            UserRepository userRepository,

            WebsiteRepository websiteRepository,

            AdminRepository adminRepository,

            ReviewVoteRepository reviewVoteRepository,

            ReportRepository reportRepository,

            BusinessResponseRepository businessResponseRepository,

            AuditLogService auditLogService) {

        this.reviewRepository = reviewRepository;

        this.userRepository = userRepository;

        this.websiteRepository = websiteRepository;

        this.adminRepository = adminRepository;

        this.reviewVoteRepository = reviewVoteRepository;

        this.reportRepository = reportRepository;

        this.businessResponseRepository = businessResponseRepository;

        this.auditLogService = auditLogService;
    }

    // ==============================
    // CREATE REVIEW
    // ==============================

    public Review saveReview(Review review) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String loggedInEmail = authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {
            throw new RuntimeException("User not found");
        }

        // Do not trust userId from frontend
        review.setUserId(loggedInUser.getId());

        // Check website exists
        if (review.getWebsiteId() == null
                || !websiteRepository.existsById(review.getWebsiteId())) {

            throw new RuntimeException("Website does not exist");
        }

        // New reviews must go through moderation
        review.setStatus("PENDING");

        return reviewRepository.save(review);
    }

    // ==============================
    // GET REVIEWS BY WEBSITE
    // ==============================

    public List<Review> getReviewsByWebsiteId(Long websiteId) {

        return reviewRepository.findByWebsiteId(websiteId);
    }

    // ==============================
    // GET ALL REVIEWS
    // ==============================

    public List<Review> getAllReviews() {

        return reviewRepository.findAll();
    }

    // ==============================
    // GET PENDING REVIEWS
    // ==============================

    public List<Review> getPendingReviews() {

        return reviewRepository.findByStatus("PENDING");
    }

    // ==============================
    // GET REVIEW BY ID
    // ==============================

    public Review getReviewById(Long id) {

        return reviewRepository.findById(id).orElse(null);
    }

    // ==============================
    // UPDATE REVIEW
    // ==============================

    public Review updateReview(Long id, Review review) {

        Review existingReview =
                reviewRepository.findById(id).orElse(null);

        if (existingReview == null) {
            return null;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String loggedInEmail = authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {
            throw new RuntimeException("User not found");
        }

        // Only owner can edit review
        if (!existingReview.getUserId().equals(loggedInUser.getId())) {

            throw new AccessDeniedException(
                    "You can edit only your own review");
        }

        // Update only rating and comment
        existingReview.setRating(review.getRating());

        existingReview.setComment(review.getComment());

        return reviewRepository.save(existingReview);
    }

    // ==============================
    // DELETE REVIEW
    // ==============================

    public void deleteReview(Long id) {

        Review existingReview =
                reviewRepository.findById(id).orElse(null);

        if (existingReview == null) {
            throw new RuntimeException("Review not found");
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String loggedInEmail = authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {
            throw new RuntimeException("User not found");
        }

        // Check admin
        boolean isAdmin =
                adminRepository.existsByUserId(loggedInUser.getId());

        // Check owner
        boolean isOwner =
                existingReview.getUserId().equals(loggedInUser.getId());

        // Only owner OR admin can delete
        if (!isOwner && !isAdmin) {

            throw new AccessDeniedException(
                    "You can delete only your own review");
        }

        // ==============================
        // DELETE DEPENDENT DATA FIRST
        // ==============================

        // 1. Delete business response
        businessResponseRepository.deleteByReviewId(id);

        // 2. Delete helpful / not helpful votes
        reviewVoteRepository.deleteByReviewId(id);

        // 3. Delete reports
        reportRepository.deleteByReviewId(id);

        // 4. Finally delete review
        reviewRepository.deleteById(id);
    }

    // ==============================
    // DELETE REVIEWS BY WEBSITE
    // ==============================

    public void deleteReviewsByWebsiteId(Long websiteId) {

        List<Review> reviews =
                reviewRepository.findByWebsiteId(websiteId);

        for (Review review : reviews) {

            Long reviewId = review.getId();

            // Delete business response
            businessResponseRepository
                    .deleteByReviewId(reviewId);

            // Delete votes
            reviewVoteRepository
                    .deleteByReviewId(reviewId);

            // Delete reports
            reportRepository
                    .deleteByReviewId(reviewId);

            // Delete review
            reviewRepository.deleteById(reviewId);
        }
    }

    // ==============================
    // ADMIN APPROVE / REJECT / HIDE / RESTORE REVIEW
    // ==============================

    public Review updateReviewStatus(Long id, String status) {

        Review review =
                reviewRepository.findById(id).orElse(null);

        if (review == null) {
            return null;
        }

        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status is required");
        }

        status = status.toUpperCase();

        if (!status.equals("PENDING")
                && !status.equals("APPROVED")
                && !status.equals("REJECTED")
                && !status.equals("HIDDEN")) {

            throw new RuntimeException(
                    "Status must be PENDING, APPROVED, REJECTED or HIDDEN");
        }

        // Store old status before changing it
        String oldStatus = review.getStatus();

        // Change review status
        review.setStatus(status);

        Review savedReview =
                reviewRepository.save(review);

        // ==============================
        // SAVE AUDIT LOG
        // ==============================

        String details =
                oldStatus + " -> " + status;

        auditLogService.log(
                "REVIEW_STATUS_CHANGED",
                "REVIEW",
                id,
                details
        );

        return savedReview;
    }

    // ==============================
    // AVERAGE RATING
    // ==============================

    public double getAverageRating(Long websiteId) {

        List<Review> reviews =
                reviewRepository.findByWebsiteId(websiteId);

        double totalRating = 0;

        int approvedCount = 0;

        for (Review review : reviews) {

            if ("APPROVED".equalsIgnoreCase(review.getStatus())) {

                totalRating += review.getRating();

                approvedCount++;
            }
        }

        if (approvedCount == 0) {
            return 0.0;
        }

        return Math.round(
                (totalRating / approvedCount) * 10.0
        ) / 10.0;
    }

    // ==============================
    // REVIEW COUNT
    // ==============================

    public int getReviewCount(Long websiteId) {

        List<Review> reviews =
                reviewRepository.findByWebsiteId(websiteId);

        int count = 0;

        for (Review review : reviews) {

            if ("APPROVED".equalsIgnoreCase(review.getStatus())) {

                count++;
            }
        }

        return count;
    }

    // ==============================
    // FIVE STAR COUNT
    // ==============================

    public int getFiveStarCount(Long websiteId) {

        return getStarCount(websiteId, 5);
    }

    // ==============================
    // FOUR STAR COUNT
    // ==============================

    public int getFourStarCount(Long websiteId) {

        return getStarCount(websiteId, 4);
    }

    // ==============================
    // THREE STAR COUNT
    // ==============================

    public int getThreeStarCount(Long websiteId) {

        return getStarCount(websiteId, 3);
    }

    // ==============================
    // TWO STAR COUNT
    // ==============================

    public int getTwoStarCount(Long websiteId) {

        return getStarCount(websiteId, 2);
    }

    // ==============================
    // ONE STAR COUNT
    // ==============================

    public int getOneStarCount(Long websiteId) {

        return getStarCount(websiteId, 1);
    }

    // ==============================
    // STAR COUNT
    // ==============================

    private int getStarCount(Long websiteId, int star) {

        List<Review> reviews =
                reviewRepository.findByWebsiteId(websiteId);

        int count = 0;

        for (Review review : reviews) {

            if ("APPROVED".equalsIgnoreCase(review.getStatus())
                    && review.getRating() != null
                    && review.getRating() == star) {

                count++;
            }
        }

        return count;
    }
}