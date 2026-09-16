package com.nit.business;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nit.Website.Website;
import com.nit.review.Review;
import com.nit.review.ReviewService;
import com.nit.user.User;
import com.nit.user.UserRepository;

@RestController
@RequestMapping({"/business-dashboard", "/api/v1/business"})
public class BusinessDashboardController {

    private final BusinessClamService businessClaimService;
    private final BusinessService businessService;
    private final ReviewService reviewService;
    private final BusinessDashboardService dashboardService;
    private final UserRepository userRepository;

    public BusinessDashboardController(
            BusinessClamService businessClaimService,
            BusinessService businessService,
            ReviewService reviewService,
            BusinessDashboardService dashboardService,
            UserRepository userRepository) {

        this.businessClaimService = businessClaimService;
        this.businessService = businessService;
        this.reviewService = reviewService;
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    // PDF API: GET /api/v1/business/dashboard
    @GetMapping("/dashboard")
    public Map<String, Object> getBusinessDashboard() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        String loggedInEmail =
                authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {
            throw new RuntimeException("User not found");
        }

        return dashboardService
                .getDashboardMetrics(loggedInUser.getId());
    }

    // Logged-in user can access only their own business
    @GetMapping("/user/{userId}")
    public Business getBusinessForUser(
            @PathVariable Long userId) {

        BusinessClaim claim =
                businessClaimService
                        .getApprovedClaimByUserId(userId);

        if (claim == null) {
            return null;
        }

        return businessService.getBusinessById(
                claim.getBusinessId());
    }

    // Logged-in user can access only their own website
    @GetMapping("/user/{userId}/website")
    public Website getWebsiteForUser(
            @PathVariable Long userId) {

        BusinessClaim claim =
                businessClaimService
                        .getApprovedClaimByUserId(userId);

        if (claim == null) {
            return null;
        }

        return businessService.getWebsiteForBusiness(
                claim.getBusinessId());
    }

    // Logged-in user can access only reviews of their business
    @GetMapping("/user/{userId}/reviews")
    public List<Review> getBusinessReviews(
            @PathVariable Long userId) {

        BusinessClaim claim =
                businessClaimService
                        .getApprovedClaimByUserId(userId);

        if (claim == null) {
            return List.of();
        }

        Website website =
                businessService.getWebsiteForBusiness(
                        claim.getBusinessId());

        if (website == null) {
            return List.of();
        }

        return reviewService.getReviewsByWebsiteId(
                website.getId());
    }

    // Existing dashboard metrics endpoint
    @GetMapping("/user/{userId}/metrics")
    public Map<String, Object> getDashboardMetrics(
            @PathVariable Long userId) {

        return dashboardService
                .getDashboardMetrics(userId);
    }
}