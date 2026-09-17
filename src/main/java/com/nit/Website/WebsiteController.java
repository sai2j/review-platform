package com.nit.Website;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nit.dto.ReviewRequestDTO;
import com.nit.dto.ReviewResponseDTO;
import com.nit.dto.WebsiteResponseDTO;
import com.nit.review.ReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/websites", "/api/v1/websites"})
public class WebsiteController {


private final WebsiteService websiteService;
private final ReviewService reviewService;

public WebsiteController(
        WebsiteService websiteService,
        ReviewService reviewService) {

    this.websiteService = websiteService;
    this.reviewService = reviewService;
}

@PostMapping
public Website createWebsite(@Valid @RequestBody Website website) {

    return websiteService.saveWebsite(website);
}

@GetMapping
public List<WebsiteResponseDTO> getAllWebsites() {

    return websiteService.getAllWebsites();
}

// WEBSITE SEARCH
@GetMapping("/search")
public List<WebsiteResponseDTO> searchWebsites(
        @RequestParam String q) {

    return websiteService.searchWebsites(q);
}

// WEBSITE PROFILE BY DOMAIN
@GetMapping("/domain/{domain}")
public WebsiteResponseDTO getWebsiteByDomain(
        @PathVariable String domain) {

    return websiteService.getWebsiteByDomain(domain);
}

// WEBSITE PROFILE BY ID
@GetMapping("/{id}")
public WebsiteResponseDTO getwebsiteById(
        @PathVariable Long id) {

    return websiteService.getWebsiteById(id);
}

// RELATED WEBSITES
@GetMapping("/{id}/related")
public List<WebsiteResponseDTO> getRelatedWebsites(
        @PathVariable Long id) {

    return websiteService.getRelatedWebsites(id);
}

// WEBSITE REVIEWS
@GetMapping("/{id}/reviews")
public List<ReviewResponseDTO> getWebsiteReviews(
        @PathVariable Long id) {

    return websiteService.getWebsiteReviews(id);
}

// CREATE REVIEW FOR WEBSITE
@PostMapping("/{id}/reviews")
public ReviewResponseDTO createReviewForWebsite(
        @PathVariable Long id,
        @Valid @RequestBody ReviewRequestDTO request) {

    request.setWebsiteId(id);

    return reviewService.saveReview(request);
}

// ADMIN SEO CONTROLS
@PreAuthorize("hasRole('ADMIN')")
@PutMapping("/{id}/seo")
public Website updateSeo(
        @PathVariable Long id,
        @RequestParam(required = false) String seoTitle,
        @RequestParam(required = false) String seoDescription,
        @RequestParam(required = false) String canonicalUrl) {

    return websiteService.updateSeo(
            id,
            seoTitle,
            seoDescription,
            canonicalUrl
    );
}

@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public String deleteWebsite(@PathVariable Long id) {

    websiteService.deleteWebsite(id);

    return " website delete sucessfully";
}


}
