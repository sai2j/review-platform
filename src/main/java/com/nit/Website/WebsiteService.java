
package com.nit.Website;

import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nit.dto.WebsiteResponseDTO;
import com.nit.review.Review;
import com.nit.review.ReviewService;

@Service
public class WebsiteService {

    private final WebsiteRepository websiteRepository;

    private final ReviewService reviewService;

    public WebsiteService(
            WebsiteRepository websiteRepository,
            ReviewService reviewService) {

        this.websiteRepository = websiteRepository;
        this.reviewService = reviewService;
    }

    public Website saveWebsite(Website website) {

        String canonicalDomain = normalizeDomain(website.getUrl());

        Website existingWebsite =
                websiteRepository.findByCanonicalDomain(canonicalDomain)
                                  .orElse(null);

        if (existingWebsite != null) {
            return existingWebsite;
        }

        website.setCanonicalDomain(canonicalDomain);

        return websiteRepository.save(website);
    }

    public List<WebsiteResponseDTO> getAllWebsites() {

        return websiteRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public WebsiteResponseDTO getWebsiteById(Long id) {

        Website website =
                websiteRepository.findById(id).orElse(null);

        if (website == null) {
            return null;
        }

        return convertToResponseDTO(website);
    }

    // WEBSITE PROFILE BY DOMAIN
    public WebsiteResponseDTO getWebsiteByDomain(String domain) {

        String canonicalDomain =
                normalizeDomain(domain);

        Website website =
                websiteRepository
                        .findByCanonicalDomain(canonicalDomain)
                        .orElse(null);

        if (website == null) {
            return null;
        }

        return convertToResponseDTO(website);
    }

    // WEBSITE REVIEWS
    public List<com.nit.dto.ReviewResponseDTO> getWebsiteReviews(
            Long websiteId) {

        List<Review> reviews =
                reviewService.getReviewsByWebsiteId(websiteId);

        return reviewService.convertToResponseDTOList(
                reviews);
    }

    // WEBSITE SEARCH
    public List<WebsiteResponseDTO> searchWebsites(String query) {

        List<Website> websites;

        if (query == null || query.trim().isEmpty()) {

            websites = websiteRepository.findAll();

        } else {

            String searchQuery = query.trim();

            websites =
                    websiteRepository
                            .findByNameContainingIgnoreCaseOrCanonicalDomainContainingIgnoreCase(
                                    searchQuery,
                                    searchQuery
                            );
        }

        return websites
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    // ADMIN SEO UPDATE
    public Website updateSeo(
            Long id,
            String seoTitle,
            String seoDescription,
            String canonicalUrl) {

        Website website =
                websiteRepository.findById(id).orElse(null);

        if (website == null) {
            throw new RuntimeException("Website not found");
        }

        website.setSeoTitle(seoTitle);
        website.setSeoDescription(seoDescription);
        website.setCanonicalUrl(canonicalUrl);

        return websiteRepository.save(website);
    }

    public void deleteWebsite(Long id) {

        // Delete all reviews linked to this website first
        reviewService.deleteReviewsByWebsiteId(id);

        // Then delete the website
        websiteRepository.deleteById(id);
    }

    // ENTITY TO DTO CONVERSION
    private WebsiteResponseDTO convertToResponseDTO(Website website) {

        return new WebsiteResponseDTO(
                website.getId(),
                website.getName(),
                website.getUrl(),
                website.getDescription(),
                website.getCanonicalDomain(),
                website.getSeoTitle(),
                website.getSeoDescription(),
                website.getCanonicalUrl()
        );
    }

    private String normalizeDomain(String url) {

        try {

            String cleanUrl = url.trim();

            if (!cleanUrl.startsWith("http://")
                    && !cleanUrl.startsWith("https://")) {

                cleanUrl = "https://" + cleanUrl;
            }

            URI uri = new URI(cleanUrl);

            String domain = uri.getHost();

            if (domain == null) {
                throw new RuntimeException("Invalid website URL");
            }

            domain = domain.toLowerCase();

            if (domain.startsWith("www.")) {
                domain = domain.substring(4);
            }

            return domain;

        } catch (Exception e) {

            throw new RuntimeException("Invalid website URL");
        }
    }
}

