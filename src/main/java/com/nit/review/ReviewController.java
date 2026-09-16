package com.nit.review;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nit.dto.ReviewRequestDTO;
import com.nit.dto.ReviewResponseDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/reviews", "/api/v1/reviews"})
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ==============================
    // GET REVIEWS BY WEBSITE
    // ==============================

    @GetMapping("/website/{websiteId}")
    public List<ReviewResponseDTO> getReviewsByWebsiteId(
            @PathVariable Long websiteId) {

        return reviewService.convertToResponseDTOList(
                reviewService.getReviewsByWebsiteId(websiteId)
        );
    }

    // ==============================
    // RATING SUMMARY
    // ==============================

    @GetMapping("/website/{websiteId}/summary")
    public RatingSummary getRatingSummary(
            @PathVariable Long websiteId) {

        return new RatingSummary(
                reviewService.getAverageRating(websiteId),
                reviewService.getReviewCount(websiteId),
                reviewService.getFiveStarCount(websiteId),
                reviewService.getFourStarCount(websiteId),
                reviewService.getThreeStarCount(websiteId),
                reviewService.getTwoStarCount(websiteId),
                reviewService.getOneStarCount(websiteId)
        );
    }

    // ==============================
    // CREATE REVIEW
    // ==============================

    @PostMapping
    public ReviewResponseDTO createReview(
            @Valid @RequestBody ReviewRequestDTO request) {

        return reviewService.saveReview(request);
    }

    // ==============================
    // CREATE REVIEW FOR WEBSITE
    // ==============================

    @PostMapping("/website/{websiteId}")
    public ReviewResponseDTO createReviewForWebsite(
            @PathVariable Long websiteId,
            @Valid @RequestBody ReviewRequestDTO request) {

        request.setWebsiteId(websiteId);

        return reviewService.saveReview(request);
    }

    // ==============================
    // GET ALL REVIEWS
    // ==============================

    @GetMapping
    public List<ReviewResponseDTO> getAllReviews() {

        return reviewService.convertToResponseDTOList(
                reviewService.getAllReviews()
        );
    }

    // ==============================
    // ADMIN PENDING REVIEW QUEUE
    // ==============================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public List<ReviewResponseDTO> getPendingReviews() {

        return reviewService.convertToResponseDTOList(
                reviewService.getPendingReviews()
        );
    }

    // ==============================
    // GET REVIEW BY ID
    // ==============================

    @GetMapping("/{id}")
    public ReviewResponseDTO getReviewById(
            @PathVariable Long id) {

        return reviewService.convertToResponseDTO(
                reviewService.getReviewById(id)
        );
    }

    // ==============================
    // UPDATE REVIEW
    // ==============================

    @PatchMapping("/{id}")
    public ReviewResponseDTO updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDTO request) {

        return reviewService.updateReview(id, request);
    }

    // ==============================
    // ADMIN APPROVE / REJECT / HIDE / RESTORE REVIEW
    // ==============================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ReviewResponseDTO updateReviewStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return reviewService.updateReviewStatus(id, status);
    }

    // ==============================
    // PDF ADMIN REVIEW MODERATION API
    // PATCH /api/v1/admin/reviews/{id}
    // ==============================

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/api/v1/admin/reviews/{id}")
    public ReviewResponseDTO adminUpdateReview(
            @PathVariable Long id,
            @RequestParam String status) {

        return reviewService.updateReviewStatus(id, status);
    }

    // ==============================
    // ADMIN REVIEW VERIFICATION STATUS
    // ==============================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/verification-status")
    public ReviewResponseDTO updateReviewVerificationStatus(
            @PathVariable Long id,
            @RequestParam String verificationStatus) {

        return reviewService.updateReviewVerificationStatus(
                id,
                verificationStatus
        );
    }

    // ==============================
    // DELETE REVIEW
    // ==============================

    @DeleteMapping("/{id}")
    public String deleteReview(
            @PathVariable Long id) {

        reviewService.deleteReview(id);

        return "Review delete Sucessfully";
    }
}