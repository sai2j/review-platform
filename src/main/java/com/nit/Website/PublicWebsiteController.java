package com.nit.Website;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import com.nit.dto.ReviewResponseDTO;
import com.nit.dto.WebsiteResponseDTO;
import com.nit.review.ReviewService;

@Controller
public class PublicWebsiteController {

	private final WebsiteService websiteService;
	private final ReviewService reviewService;

	public PublicWebsiteController(WebsiteService websiteService, ReviewService reviewService) {
		this.websiteService = websiteService;
		this.reviewService = reviewService;
	}

	@GetMapping("/website/{id}")
	public String getPublicWebsitePage(@PathVariable Long id, Model model, HttpServletRequest request) {
		WebsiteResponseDTO website = websiteService.getWebsiteById(id);
		if (website == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Website not found");
		}
		Page<ReviewResponseDTO> approvedReviews = reviewService.getApprovedReviewsByWebsiteId(id,
				PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
		String pageTitle = website.getSeoTitle();
		if (pageTitle == null || pageTitle.isBlank()) {
			pageTitle = website.getName() + " Reviews | Review Platform";
		}
		String pageDescription = website.getSeoDescription();
		if (pageDescription == null || pageDescription.isBlank()) {
			pageDescription = website.getDescription();
		}
		if (pageDescription == null || pageDescription.isBlank()) {
			pageDescription = "Read reviews and ratings for " + website.getName() + " on Review Platform.";
		}
		String canonicalUrl = website.getCanonicalUrl();
		if (canonicalUrl == null || canonicalUrl.isBlank()) {
			canonicalUrl = buildCurrentPageUrl(request, id);
		}
		boolean thinPage = approvedReviews.getTotalElements() == 0
				&& (website.getDescription() == null || website.getDescription().isBlank());
		model.addAttribute("website", website);
		model.addAttribute("reviews", approvedReviews.getContent());
		model.addAttribute("averageRating", website.getAverageRating());
		model.addAttribute("reviewCount", website.getReviewCount());
		model.addAttribute("pageTitle", pageTitle);
		model.addAttribute("pageDescription", pageDescription);
		model.addAttribute("canonicalUrl", canonicalUrl);
		model.addAttribute("thinPage", thinPage);
		model.addAttribute("currentUrl", canonicalUrl);
		return "website";
	}

	private String buildCurrentPageUrl(HttpServletRequest request, Long id) {
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String contextPath = request.getContextPath();
		StringBuilder url = new StringBuilder();
		url.append(scheme).append("://").append(serverName);
		if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
			url.append(":").append(serverPort);
		}
		url.append(contextPath).append("/website/").append(id);
		return url.toString();
	}
}