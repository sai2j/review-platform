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

        this.businessClaimService = businessClaimService;
        this.businessService = businessService;
        this.reviewRepository = reviewRepository;
        this.businessResponseRepository = businessResponseRepository;
    }

    public Map<String, Object> getDashboardMetrics(Long userId) {

        BusinessClaim claim =
                businessClaimService.getApprovedClaimByUserId(userId);

        Map<String, Object> metrics =
                new LinkedHashMap<>();

        if (claim == null) {
            metrics.put("reviewCount", 0);
            metrics.put("averageRating", 0.0);
            metrics.put("responseRate", 0.0);
            metrics.put("ratingTrend", List.of());
            return metrics;
        }

        Website website =
                businessService.getWebsiteForBusiness(
                        claim.getBusinessId());

        if (website == null) {
            metrics.put("reviewCount", 0);
            metrics.put("averageRating", 0.0);
            metrics.put("responseRate", 0.0);
            metrics.put("ratingTrend", List.of());
            return metrics;
        }

        List<Review> allReviews =
                reviewRepository.findByWebsiteId(
                        website.getId());

        // Only approved/published reviews are counted
        List<Review> approvedReviews =
                new ArrayList<>();

        for (Review review : allReviews) {

            if ("APPROVED".equalsIgnoreCase(
                    review.getStatus())) {

                approvedReviews.add(review);
            }
        }

        int reviewCount =
                approvedReviews.size();

        double totalRating = 0.0;

        for (Review review : approvedReviews) {

            totalRating +=
                    review.getRating() != null
                            ? review.getRating()
                            : 0;
        }

        double averageRating =
                reviewCount == 0
                        ? 0.0
                        : totalRating / reviewCount;

        // Response rate
        int responseCount = 0;

        List<BusinessResponse> responses =
                businessResponseRepository.findAll();

        for (BusinessResponse response : responses) {

            if (!claim.getBusinessId()
                    .equals(response.getBusinessId())) {

                continue;
            }

            for (Review review : approvedReviews) {

                if (review.getId()
                        .equals(response.getReviewId())) {

                    responseCount++;
                    break;
                }
            }
        }

        double responseRate =
                reviewCount == 0
                        ? 0.0
                        : (responseCount * 100.0)
                                / reviewCount;

        // Rating trend by month
        Map<String, List<Integer>> monthlyRatings =
                new LinkedHashMap<>();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM");

        for (Review review : approvedReviews) {

            LocalDateTime createdAt =
                    review.getCreatedAt();

            if (createdAt == null) {
                continue;
            }

            String month =
                    createdAt.format(formatter);

            monthlyRatings
                    .computeIfAbsent(
                            month,
                            key -> new ArrayList<>())
                    .add(review.getRating());
        }

        List<Map<String, Object>> ratingTrend =
                new ArrayList<>();

        for (Map.Entry<String, List<Integer>> entry
                : monthlyRatings.entrySet()) {

            List<Integer> ratings =
                    entry.getValue();

            double monthlyTotal = 0.0;

            for (Integer rating : ratings) {
                monthlyTotal += rating;
            }

            double monthlyAverage =
                    ratings.isEmpty()
                            ? 0.0
                            : monthlyTotal / ratings.size();

            Map<String, Object> trend =
                    new LinkedHashMap<>();

            trend.put("month", entry.getKey());
            trend.put(
                    "averageRating",
                    Math.round(monthlyAverage * 100.0)
                            / 100.0);
            trend.put(
                    "reviewCount",
                    ratings.size());

            ratingTrend.add(trend);
        }

        metrics.put("reviewCount", reviewCount);
        metrics.put(
                "averageRating",
                Math.round(averageRating * 100.0)
                        / 100.0);
        metrics.put(
                "responseRate",
                Math.round(responseRate * 100.0)
                        / 100.0);
        metrics.put("ratingTrend", ratingTrend);

        return metrics;
    }
}