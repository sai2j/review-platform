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
@RequestMapping({ "/business-dashboard", "/api/v1/business" })
public class BusinessDashboardController {

	private final BusinessClamService businessClaimService;
	private final BusinessService businessService;
	private final ReviewService reviewService;
	private final BusinessDashboardService dashboardService;
	private final UserRepository userRepository;

	public BusinessDashboardController(BusinessClamService businessClaimService, BusinessService businessService,
			ReviewService reviewService, BusinessDashboardService dashboardService, UserRepository userRepository) {
		this.businessClaimService = businessClaimService;
		this.businessService = businessService;
		this.reviewService = reviewService;
		this.dashboardService = dashboardService;
		this.userRepository = userRepository;
	}

	@GetMapping("/dashboard")
	public Map<String, Object> getBusinessDashboard() {

		User loggedInUser = getLoggedInUser();

		getVerifiedApprovedClaim(loggedInUser.getId());

		return dashboardService.getDashboardMetrics(loggedInUser.getId());
	}

	@GetMapping("/user/{userId}")
	public Business getBusinessForUser(@PathVariable Long userId) {

		User loggedInUser = getLoggedInUser();

		if (!loggedInUser.getId().equals(userId)) {
			throw new org.springframework.security.access.AccessDeniedException(
					"You can access only your own business dashboard");
		}

		BusinessClaim claim = getVerifiedApprovedClaim(loggedInUser.getId());

		if (claim == null) {
			return null;
		}

		return businessService.getBusinessById(claim.getBusinessId());
	}

	@GetMapping("/user/{userId}/website")
	public Website getWebsiteForUser(@PathVariable Long userId) {

		User loggedInUser = getLoggedInUser();
		if (!loggedInUser.getId().equals(userId)) {
			throw new org.springframework.security.access.AccessDeniedException(
					"You can access only your own business dashboard");
		}

		BusinessClaim claim = getVerifiedApprovedClaim(loggedInUser.getId());

		if (claim == null) {
			return null;
		}

		return businessService.getWebsiteForBusiness(claim.getBusinessId());
	}

	@GetMapping("/user/{userId}/reviews")
	public List<Review> getBusinessReviews(@PathVariable Long userId) {

		User loggedInUser = getLoggedInUser();
		if (!loggedInUser.getId().equals(userId)) {
			throw new org.springframework.security.access.AccessDeniedException(
					"You can access only your own business dashboard");
		}

		BusinessClaim claim = getVerifiedApprovedClaim(loggedInUser.getId());

		if (claim == null) {
			return List.of();
		}

		Website website = businessService.getWebsiteForBusiness(claim.getBusinessId());

		if (website == null) {
			return List.of();
		}

		return reviewService.getReviewsByWebsiteId(website.getId());
	}

	@GetMapping("/user/{userId}/metrics")
	public Map<String, Object> getDashboardMetrics(@PathVariable Long userId) {

		User loggedInUser = getLoggedInUser();

		if (!loggedInUser.getId().equals(userId)) {
			throw new org.springframework.security.access.AccessDeniedException(
					"You can access only your own business dashboard");
		}

		getVerifiedApprovedClaim(loggedInUser.getId());
		return dashboardService.getDashboardMetrics(loggedInUser.getId());
	}

	private User getLoggedInUser() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new org.springframework.security.access.AccessDeniedException("User is not authenticated");
		}

		String loggedInEmail = authentication.getName();

		User loggedInUser = userRepository.findByEmail(loggedInEmail);

		if (loggedInUser == null) {
			throw new RuntimeException("User not found");
		}

		return loggedInUser;
	}

	private BusinessClaim getVerifiedApprovedClaim(Long userId) {

		BusinessClaim claim = businessClaimService.getApprovedClaimByUserId(userId);
		if (claim == null) {
			throw new org.springframework.security.access.AccessDeniedException("Business ownership is not approved");
		}

		Business business = businessService.getBusinessById(claim.getBusinessId());

		if (business == null) {
			throw new RuntimeException("Business not found");
		}

		if (!"VERIFIED".equalsIgnoreCase(business.getStatus())) {
			throw new org.springframework.security.access.AccessDeniedException(
					"Business ownership verification is required before accessing the dashboard");
		}

		return claim;
	}
}