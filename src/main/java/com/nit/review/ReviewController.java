package com.nit.review;

import java.time.LocalDate;

import java.util.List;

import java.util.Set;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Sort;

import org.springframework.data.web.PageableDefault;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PatchMapping;

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

@RequestMapping({ "/reviews", "/api/v1/reviews" })

@Validated

public class ReviewController {

    private final ReviewService reviewService;

    private static final Set<String> ALLOWED_SORT_FIELDS =

            Set.of("createdAt", "rating");

    public ReviewController(ReviewService reviewService) {

        this.reviewService = reviewService;

    }

    // ==============================

    // GET REVIEWS BY WEBSITE

    // ==============================

    @GetMapping("/website/{websiteId}")

    public Page<ReviewResponseDTO> getReviewsByWebsiteId(

            @PathVariable Long websiteId,

            @RequestParam(required = false) Integer rating,

            @RequestParam(required = false) String verificationStatus,

            @RequestParam(required = false) String experienceType,

            @RequestParam(required = false) Integer experienceRating,

            @RequestParam(required = false)

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)

            LocalDate startDate,

            @RequestParam(required = false)

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)

            LocalDate endDate,

            @PageableDefault(

                    size = 10,

                    sort = "createdAt",

                    direction = Sort.Direction.DESC

            )

            Pageable pageable) {

        pageable = validateSorting(pageable);

        if (experienceType != null && !experienceType.isBlank()) {

            return reviewService.getReviewsByWebsiteIdAndExperienceType(

                    websiteId,

                    experienceType,

                    experienceRating,

                    pageable

            );

        }

        if (rating != null) {

            return reviewService.getReviewsByWebsiteIdAndRating(

                    websiteId,

                    rating,

                    pageable

            );

        }

        if (verificationStatus != null

                && !verificationStatus.isBlank()) {

            return reviewService.getReviewsByWebsiteIdAndVerificationStatus(

                    websiteId,

                    verificationStatus,

                    pageable

            );

        }

        if (startDate != null || endDate != null) {

            return reviewService.getReviewsByWebsiteIdAndDate(

                    websiteId,

                    startDate,

                    endDate,

                    pageable

            );

        }

        /*
         * Public website review listing:
         * show reviews returned by ReviewService.
         *
         * This allows PENDING and APPROVED reviews
         * to appear in the Customer Reviews section.
         */

        return reviewService.getReviewsByWebsiteId(

                websiteId,

                pageable

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

    public Page<ReviewResponseDTO> getAllReviews(

            @PageableDefault(

                    size = 10,

                    sort = "createdAt",

                    direction = Sort.Direction.DESC

            )

            Pageable pageable) {

        pageable = validateSorting(pageable);

        return reviewService.getAllReviews(pageable);

    }

    // ==============================

    // GET PENDING REVIEWS

    // ==============================

    @PreAuthorize("hasRole('ADMIN')")

    @GetMapping("/pending")

    public List<ReviewResponseDTO> getPendingReviews() {

        return reviewService.convertToResponseDTOList(

                reviewService.getPendingReviews()

        );

    }

    // ==============================

    // GET HIDDEN REVIEWS

    // ==============================

    @PreAuthorize("hasRole('ADMIN')")

    @GetMapping("/hidden")

    public List<ReviewResponseDTO> getHiddenReviews() {

        return reviewService.convertToResponseDTOList(

                reviewService.getHiddenReviews()

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

    // UPDATE OWN REVIEW - PATCH

    // ==============================

    @PatchMapping("/{id}")

    public ReviewResponseDTO updateReview(

            @PathVariable Long id,

            @Valid @RequestBody ReviewRequestDTO request) {

        return reviewService.updateReview(id, request);

    }

    // ==============================

    // UPDATE OWN REVIEW - PUT

    // ==============================

    // Added because current frontend was using PUT.

    // Existing PATCH endpoint is also kept.

    @PutMapping("/{id}")

    public ReviewResponseDTO updateReviewWithPut(

            @PathVariable Long id,

            @Valid @RequestBody ReviewRequestDTO request) {

        return reviewService.updateReview(id, request);

    }

    // ==============================

    // UPDATE REVIEW STATUS - ADMIN

    // ==============================

    @PreAuthorize("hasRole('ADMIN')")

    @PutMapping("/{id}/status")

    public ReviewResponseDTO updateReviewStatus(

            @PathVariable Long id,

            @RequestParam String status) {

        return reviewService.updateReviewStatus(id, status);

    }

    // ==============================

    // ADMIN REVIEW UPDATE

    // ==============================

    @PreAuthorize("hasRole('ADMIN')")

    @PatchMapping("/api/v1/admin/reviews/{id}")

    public ReviewResponseDTO adminUpdateReview(

            @PathVariable Long id,

            @RequestParam String status) {

        return reviewService.updateReviewStatus(id, status);

    }

    // ==============================

    // UPDATE VERIFICATION STATUS

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

    // ==============================

    // VALIDATE SORTING

    // ==============================

    private Pageable validateSorting(Pageable pageable) {

        if (pageable.getSort().isUnsorted()) {

            return pageable;

        }

        for (Sort.Order order : pageable.getSort()) {

            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {

                throw new RuntimeException(

                        "Sorting is allowed only by: createdAt, rating"

                );

            }

        }

        return PageRequest.of(

                pageable.getPageNumber(),

                pageable.getPageSize(),

                pageable.getSort()

        );

    }

}