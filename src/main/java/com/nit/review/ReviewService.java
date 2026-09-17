package com.nit.review;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nit.Report.ReportRepository;
import com.nit.Website.WebsiteRepository;
import com.nit.admin.AdminRepository;
import com.nit.audit.AuditLogService;
import com.nit.business.BusinessResponseRepository;
import com.nit.dto.ReviewRequestDTO;
import com.nit.dto.ReviewResponseDTO;
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

    public ReviewResponseDTO saveReview(ReviewRequestDTO request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String loggedInEmail = authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {
            throw new RuntimeException("User not found");
        }

        if (request.getWebsiteId() == null
                || !websiteRepository.existsById(request.getWebsiteId())) {

            throw new RuntimeException("Website does not exist");
        }

        Review review = new Review();

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review.setUserId(loggedInUser.getId());
        review.setWebsiteId(request.getWebsiteId());

        review.setDeliveryRating(request.getDeliveryRating());
        review.setSupportRating(request.getSupportRating());
        review.setRefundRating(request.getRefundRating());
        review.setProductRating(request.getProductRating());
        review.setPricingRating(request.getPricingRating());

        review.setStatus("PENDING");
        review.setVerificationStatus("UNVERIFIED");

        Review savedReview =
                reviewRepository.save(review);

        return convertToResponseDTO(savedReview);
    }

    // ==============================
    // GET REVIEWS BY WEBSITE
    // ==============================

    public List<Review> getReviewsByWebsiteId(Long websiteId) {
        return reviewRepository.findByWebsiteId(websiteId);
    }

    // ==============================
    // PAGINATED REVIEWS BY WEBSITE
    // ==============================

    public Page<ReviewResponseDTO> getReviewsByWebsiteId(
            Long websiteId,
            Pageable pageable) {

        return reviewRepository
                .findByWebsiteId(websiteId, pageable)
                .map(this::convertToResponseDTO);
    }

    // ==============================
    // RATING FILTER + PAGINATION
    // ==============================

    public Page<ReviewResponseDTO> getReviewsByWebsiteIdAndRating(
            Long websiteId,
            Integer rating,
            Pageable pageable) {

        return reviewRepository
                .findByWebsiteIdAndRating(
                        websiteId,
                        rating,
                        pageable)
                .map(this::convertToResponseDTO);
    }

    // ==============================
    // VERIFICATION FILTER + PAGINATION
    // ==============================

    public Page<ReviewResponseDTO>
    getReviewsByWebsiteIdAndVerificationStatus(
            Long websiteId,
            String verificationStatus,
            Pageable pageable) {

        return reviewRepository
                .findByWebsiteIdAndVerificationStatus(
                        websiteId,
                        verificationStatus,
                        pageable)
                .map(this::convertToResponseDTO);
    }

    // ==============================
    // EXPERIENCE TYPE FILTER
    // ==============================

    public Page<ReviewResponseDTO>
    getReviewsByWebsiteIdAndExperienceType(
            Long websiteId,
            String experienceType,
            Integer experienceRating,
            Pageable pageable) {

        if (experienceType == null
                || experienceType.isBlank()) {

            throw new RuntimeException(
                    "Experience type is required");
        }

        if (experienceRating == null
                || experienceRating < 1
                || experienceRating > 5) {

            throw new RuntimeException(
                    "Experience rating must be between 1 and 5");
        }

        String type =
                experienceType.trim().toLowerCase();

        Page<Review> reviews;

        switch (type) {

            case "delivery":

                reviews = reviewRepository
                        .findByWebsiteIdAndDeliveryRating(
                                websiteId,
                                experienceRating,
                                pageable);
                break;

            case "support":

                reviews = reviewRepository
                        .findByWebsiteIdAndSupportRating(
                                websiteId,
                                experienceRating,
                                pageable);
                break;

            case "refund":

                reviews = reviewRepository
                        .findByWebsiteIdAndRefundRating(
                                websiteId,
                                experienceRating,
                                pageable);
                break;

            case "product":

                reviews = reviewRepository
                        .findByWebsiteIdAndProductRating(
                                websiteId,
                                experienceRating,
                                pageable);
                break;

            case "pricing":

                reviews = reviewRepository
                        .findByWebsiteIdAndPricingRating(
                                websiteId,
                                experienceRating,
                                pageable);
                break;

            default:

                throw new RuntimeException(
                        "Experience type must be delivery, support, refund, product or pricing");
        }

        return reviews.map(this::convertToResponseDTO);
    }

    // ==============================
    // DATE FILTER + PAGINATION
    // ==============================

    public Page<ReviewResponseDTO>
    getReviewsByWebsiteIdAndDate(
            Long websiteId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        if (startDate == null && endDate == null) {

            throw new RuntimeException(
                    "At least one date is required");
        }

        if (startDate != null
                && endDate != null
                && startDate.isAfter(endDate)) {

            throw new RuntimeException(
                    "Start date cannot be after end date");
        }

        LocalDateTime startDateTime;

        LocalDateTime endDateTime;

        if (startDate != null) {

            startDateTime =
                    startDate.atStartOfDay();

        } else {

            startDateTime =
                    LocalDate.of(1900, 1, 1)
                            .atStartOfDay();
        }

        if (endDate != null) {

            endDateTime =
                    endDate.atTime(
                            23,
                            59,
                            59,
                            999999999);

        } else {

            endDateTime =
                    LocalDate.of(9999, 12, 31)
                            .atTime(
                                    23,
                                    59,
                                    59,
                                    999999999);
        }

        return reviewRepository
                .findByWebsiteIdAndCreatedAtBetween(
                        websiteId,
                        startDateTime,
                        endDateTime,
                        pageable)
                .map(this::convertToResponseDTO);
    }

    // ==============================
    // GET ALL REVIEWS
    // ==============================

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // ==============================
    // PAGINATED ALL REVIEWS
    // ==============================

    public Page<ReviewResponseDTO> getAllReviews(
            Pageable pageable) {

        return reviewRepository
                .findAll(pageable)
                .map(this::convertToResponseDTO);
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

        return reviewRepository
                .findById(id)
                .orElse(null);
    }

    // ==============================
    // UPDATE REVIEW
    // ==============================

    public ReviewResponseDTO updateReview(
            Long id,
            ReviewRequestDTO request) {

        Review existingReview =
                reviewRepository
                        .findById(id)
                        .orElse(null);

        if (existingReview == null) {
            return null;
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String loggedInEmail =
                authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {
            throw new RuntimeException("User not found");
        }

        if (!existingReview.getUserId()
                .equals(loggedInUser.getId())) {

            throw new AccessDeniedException(
                    "You can edit only your own review");
        }

        existingReview.setRating(
                request.getRating());

        existingReview.setComment(
                request.getComment());

        existingReview.setDeliveryRating(
                request.getDeliveryRating());

        existingReview.setSupportRating(
                request.getSupportRating());

        existingReview.setRefundRating(
                request.getRefundRating());

        existingReview.setProductRating(
                request.getProductRating());

        existingReview.setPricingRating(
                request.getPricingRating());

        Review savedReview =
                reviewRepository.save(existingReview);

        return convertToResponseDTO(savedReview);
    }

    // ==============================
    // DELETE REVIEW
    // ==============================

    public void deleteReview(Long id) {

        Review existingReview =
                reviewRepository
                        .findById(id)
                        .orElse(null);

        if (existingReview == null) {
            throw new RuntimeException("Review not found");
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String loggedInEmail =
                authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {
            throw new RuntimeException("User not found");
        }

        boolean isAdmin =
                adminRepository.existsByUserId(
                        loggedInUser.getId());

        boolean isOwner =
                existingReview.getUserId()
                        .equals(loggedInUser.getId());

        if (!isOwner && !isAdmin) {

            throw new AccessDeniedException(
                    "You can delete only your own review");
        }

        businessResponseRepository
                .deleteByReviewId(id);

        reviewVoteRepository
                .deleteByReviewId(id);

        reportRepository
                .deleteByReviewId(id);

        reviewRepository.deleteById(id);
    }

    // ==============================
    // DELETE REVIEWS BY WEBSITE
    // ==============================

    public void deleteReviewsByWebsiteId(Long websiteId) {

        List<Review> reviews =
                reviewRepository
                        .findByWebsiteId(websiteId);

        for (Review review : reviews) {

            Long reviewId =
                    review.getId();

            businessResponseRepository
                    .deleteByReviewId(reviewId);

            reviewVoteRepository
                    .deleteByReviewId(reviewId);

            reportRepository
                    .deleteByReviewId(reviewId);

            reviewRepository
                    .deleteById(reviewId);
        }
    }

    // ==============================
    // ADMIN APPROVE / REJECT / HIDE / RESTORE
    // ==============================

    public ReviewResponseDTO updateReviewStatus(
            Long id,
            String status) {

        Review review =
                reviewRepository
                        .findById(id)
                        .orElse(null);

        if (review == null) {
            return null;
        }

        if (status == null
                || status.isBlank()) {

            throw new RuntimeException(
                    "Status is required");
        }

        status =
                status.toUpperCase();

        if (!status.equals("PENDING")
                && !status.equals("APPROVED")
                && !status.equals("REJECTED")
                && !status.equals("HIDDEN")) {

            throw new RuntimeException(
                    "Status must be PENDING, APPROVED, REJECTED or HIDDEN");
        }

        String oldStatus =
                review.getStatus();

        review.setStatus(status);

        Review savedReview =
                reviewRepository.save(review);

        auditLogService.log(
                "REVIEW_STATUS_CHANGED",
                "REVIEW",
                id,
                oldStatus + " -> " + status
        );

        return convertToResponseDTO(savedReview);
    }

    // ==============================
    // ADMIN REVIEW VERIFICATION STATUS
    // ==============================

    public ReviewResponseDTO updateReviewVerificationStatus(
            Long id,
            String verificationStatus) {

        Review review =
                reviewRepository
                        .findById(id)
                        .orElse(null);

        if (review == null) {
            return null;
        }

        if (verificationStatus == null
                || verificationStatus.isBlank()) {

            throw new RuntimeException(
                    "Verification status is required");
        }

        verificationStatus =
                verificationStatus.toUpperCase();

        if (!verificationStatus.equals("UNVERIFIED")
                && !verificationStatus.equals("EMAIL_VERIFIED")
                && !verificationStatus.equals("EXPERIENCE_VERIFIED")
                && !verificationStatus.equals("UNDER_REVIEW")
                && !verificationStatus.equals("REMOVED")) {

            throw new RuntimeException(
                    "Verification status must be UNVERIFIED, EMAIL_VERIFIED, EXPERIENCE_VERIFIED, UNDER_REVIEW or REMOVED");
        }

        String oldVerificationStatus =
                review.getVerificationStatus();

        review.setVerificationStatus(
                verificationStatus);

        Review savedReview =
                reviewRepository.save(review);

        auditLogService.log(
                "REVIEW_VERIFICATION_STATUS_CHANGED",
                "REVIEW",
                id,
                oldVerificationStatus
                        + " -> "
                        + verificationStatus
        );

        return convertToResponseDTO(savedReview);
    }

    // ==============================
    // AVERAGE RATING
    // ==============================

    public double getAverageRating(Long websiteId) {

        Double average =
                reviewRepository
                        .findAverageApprovedRating(websiteId);

        if (average == null) {
            return 0.0;
        }

        return Math.round(
                average * 10.0
        ) / 10.0;
    }

    // ==============================
    // REVIEW COUNT
    // ==============================

    public int getReviewCount(Long websiteId) {

        Long count =
                reviewRepository
                        .countApprovedReviews(websiteId);

        return count == null
                ? 0
                : count.intValue();
    }

    // ==============================
    // STAR COUNTS
    // ==============================

    public int getFiveStarCount(Long websiteId) {
        return getStarCount(websiteId, 5);
    }

    public int getFourStarCount(Long websiteId) {
        return getStarCount(websiteId, 4);
    }

    public int getThreeStarCount(Long websiteId) {
        return getStarCount(websiteId, 3);
    }

    public int getTwoStarCount(Long websiteId) {
        return getStarCount(websiteId, 2);
    }

    public int getOneStarCount(Long websiteId) {
        return getStarCount(websiteId, 1);
    }

    private int getStarCount(
            Long websiteId,
            int star) {

        Long count =
                reviewRepository
                        .countApprovedReviewsByRating(
                                websiteId,
                                star);

        return count == null
                ? 0
                : count.intValue();
    }

    // ==============================
    // ENTITY -> RESPONSE DTO
    // ==============================

    public ReviewResponseDTO convertToResponseDTO(
            Review review) {

        if (review == null) {
            return null;
        }

        return new ReviewResponseDTO(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getUserId(),
                review.getWebsiteId(),
                review.getStatus(),
                review.getVerificationStatus(),
                review.getDeliveryRating(),
                review.getSupportRating(),
                review.getRefundRating(),
                review.getProductRating(),
                review.getPricingRating(),
                review.getCreatedAt()
        );
    }

    // ==============================
    // ENTITY LIST -> RESPONSE DTO LIST
    // ==============================

    public List<ReviewResponseDTO> convertToResponseDTOList(
            List<Review> reviews) {

        return reviews.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}