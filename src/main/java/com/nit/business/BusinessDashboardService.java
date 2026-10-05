package com.nit.business;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nit.Website.Website;
import com.nit.review.Review;
import com.nit.review.ReviewRepository;

@Service
public class BusinessDashboardService {

    private final BusinessClamService businessClaimService;
    private final BusinessService businessService;
    private final ReviewRepository reviewRepository;
    private final BusinessResponseRepository businessResponseRepository;

    public BusinessDashboardService(
            BusinessClamService businessClaimService,
            BusinessService businessService,
            ReviewRepository reviewRepository,
            BusinessResponseRepository businessResponseRepository) {

        this.businessClaimService =
                businessClaimService;

        this.businessService =
                businessService;

        this.reviewRepository =
                reviewRepository;

        this.businessResponseRepository =
                businessResponseRepository;
    }

    public Map<String, Object> getDashboardMetrics(
            Long userId) {

        BusinessClaim claim =
                businessClaimService.getApprovedClaimByUserId(
                        userId
                );

        Map<String, Object> metrics =
                new LinkedHashMap<>();

        if (claim == null) {

            metrics.put(
                    "reviewCount",
                    0
            );

            metrics.put(
                    "averageRating",
                    0.0
            );

            metrics.put(
                    "responseRate",
                    0.0
            );

            metrics.put(
                    "ratingTrend",
                    List.of()
            );

            return metrics;
        }

        Business business =
                businessService.getBusinessById(
                        claim.getBusinessId()
                );

        if (business == null
                || !"VERIFIED".equalsIgnoreCase(
                        business.getStatus()
                )) {

            metrics.put(
                    "reviewCount",
                    0
            );

            metrics.put(
                    "averageRating",
                    0.0
            );

            metrics.put(
                    "responseRate",
                    0.0
            );

            metrics.put(
                    "ratingTrend",
                    List.of()
            );

            return metrics;
        }

        Website website =
                businessService.getWebsiteForBusiness(
                        claim.getBusinessId()
                );

        if (website == null) {

            metrics.put(
                    "reviewCount",
                    0
            );

            metrics.put(
                    "averageRating",
                    0.0
            );

            metrics.put(
                    "responseRate",
                    0.0
            );

            metrics.put(
                    "ratingTrend",
                    List.of()
            );

            return metrics;
        }

        // ==========================================
        // GET ALL REVIEWS
        // ==========================================

        List<Review> allReviews =
                reviewRepository.findByWebsiteId(
                        website.getId()
                );

        // ==========================================
        // COUNTED REVIEWS
        //
        // PENDING  -> COUNT
        // APPROVED -> COUNT
        // HIDDEN   -> IGNORE
        // REJECTED -> SHOULD BE DELETED
        // ==========================================

        List<Review> countedReviews =
                new ArrayList<>();

        for (Review review : allReviews) {

            String status =
                    review.getStatus();

            if ("PENDING".equalsIgnoreCase(status)
                    || "APPROVED".equalsIgnoreCase(status)) {

                countedReviews.add(
                        review
                );
            }
        }

        // ==========================================
        // REVIEW COUNT
        // ==========================================

        int reviewCount =
                countedReviews.size();

        // ==========================================
        // AVERAGE RATING
        // ==========================================

        double totalRating =
                0.0;

        for (Review review :
                countedReviews) {

            totalRating +=
                    review.getRating() != null
                            ? review.getRating()
                            : 0;
        }

        double averageRating =
                reviewCount == 0
                        ? 0.0
                        : totalRating / reviewCount;

        // ==========================================
        // BUSINESS RESPONSE COUNT
        // ==========================================

        int responseCount =
                0;

        List<BusinessResponse> responses =
                businessResponseRepository.findAll();

        for (BusinessResponse response :
                responses) {

            if (!claim.getBusinessId()
                    .equals(
                            response.getBusinessId()
                    )) {

                continue;
            }

            for (Review review :
                    countedReviews) {

                if (review.getId()
                        .equals(
                                response.getReviewId()
                        )) {

                    responseCount++;

                    break;
                }
            }
        }

        // ==========================================
        // RESPONSE RATE
        // ==========================================

        double responseRate =
                reviewCount == 0
                        ? 0.0
                        : (
                            responseCount * 100.0
                        ) / reviewCount;

        // ==========================================
        // RATING TREND
        // ==========================================

        Map<String, List<Integer>>
                monthlyRatings =
                new LinkedHashMap<>();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "yyyy-MM"
                );

        for (Review review :
                countedReviews) {

            LocalDateTime createdAt =
                    review.getCreatedAt();

            if (createdAt == null) {
                continue;
            }

            String month =
                    createdAt.format(
                            formatter
                    );

            monthlyRatings
                    .computeIfAbsent(
                            month,
                            key ->
                                    new ArrayList<>()
                    )
                    .add(
                            review.getRating()
                    );
        }

        List<Map<String, Object>>
                ratingTrend =
                new ArrayList<>();

        for (Map.Entry<String,
                List<Integer>> entry :
                monthlyRatings.entrySet()) {

            List<Integer> ratings =
                    entry.getValue();

            double monthlyTotal =
                    0.0;

            for (Integer rating :
                    ratings) {

                monthlyTotal +=
                        rating;
            }

            double monthlyAverage =
                    ratings.isEmpty()
                            ? 0.0
                            : monthlyTotal
                                / ratings.size();

            Map<String, Object> trend =
                    new LinkedHashMap<>();

            trend.put(
                    "month",
                    entry.getKey()
            );

            trend.put(
                    "averageRating",
                    Math.round(
                            monthlyAverage * 100.0
                    ) / 100.0
            );

            trend.put(
                    "reviewCount",
                    ratings.size()
            );

            ratingTrend.add(
                    trend
            );
        }

        // ==========================================
        // FINAL METRICS
        // ==========================================

        metrics.put(
                "reviewCount",
                reviewCount
        );

        metrics.put(
                "averageRating",
                Math.round(
                        averageRating * 100.0
                ) / 100.0
        );

        metrics.put(
                "responseRate",
                Math.round(
                        responseRate * 100.0
                ) / 100.0
        );

        metrics.put(
                "ratingTrend",
                ratingTrend
        );

        return metrics;
    }
}