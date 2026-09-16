package com.nit.admin;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nit.dto.ReviewResponseDTO;
import com.nit.review.ReviewService;

@RestController
@RequestMapping("/api/v1/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Admin: get all reviews
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ReviewResponseDTO> getAllReviews() {
        return reviewService.convertToResponseDTOList(
                reviewService.getAllReviews()
        );
    }

    // Admin review moderation
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ReviewResponseDTO updateReviewStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return reviewService.updateReviewStatus(id, status);
    }
}