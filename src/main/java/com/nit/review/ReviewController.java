package com.nit.review;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reviews")
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/website/{websiteId}")
    public List<Review> getReviewsByWebsiteId(
            @PathVariable Long websiteId) {

        return reviewService.getReviewsByWebsiteId(websiteId);
    }

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

    @PostMapping
    public Review createReview(
            @Valid @RequestBody Review review) {

        return reviewService.saveReview(review);
    }

    @GetMapping
    public List<Review> getAllReviews() {

        return reviewService.getAllReviews();
    }

    // ==============================
    // ADMIN PENDING REVIEW QUEUE
    // ==============================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public List<Review> getPendingReviews() {

        return reviewService.getPendingReviews();
    }

    @GetMapping("/{id}")
    public Review getReviewById(
            @PathVariable Long id) {

        return reviewService.getReviewById(id);
    }

    @PutMapping("/{id}")
    public Review updateReview(
            @PathVariable Long id,
            @Valid @RequestBody Review review) {

        return reviewService.updateReview(id, review);
    }

    // ==============================
    // ADMIN APPROVE / REJECT / HIDE / RESTORE REVIEW
    // ==============================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public Review updateReviewStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return reviewService.updateReviewStatus(id, status);
    }

    // ==============================
    // ADMIN REVIEW VERIFICATION STATUS
    // ==============================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/verification-status")
    public Review updateReviewVerificationStatus(
            @PathVariable Long id,
            @RequestParam String verificationStatus) {

        return reviewService.updateReviewVerificationStatus(
                id,
                verificationStatus);
    }

    @DeleteMapping("/{id}")
    public String deleteReview(
            @PathVariable Long id) {

        reviewService.deleteReview(id);

        return "Review delete Sucessfully";
    }
}