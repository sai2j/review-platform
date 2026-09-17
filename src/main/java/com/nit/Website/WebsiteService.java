
package com.nit.Website;

import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nit.business.Business;
import com.nit.business.BusinessRepository;
import com.nit.business.BusinessclaimRepository;
import com.nit.business.BusinessClaim;
import com.nit.dto.ReviewResponseDTO;
import com.nit.dto.WebsiteResponseDTO;
import com.nit.review.Review;
import com.nit.review.ReviewService;

@Service
public class WebsiteService {

    private final WebsiteRepository websiteRepository;

    private final ReviewService reviewService;

    private final BusinessRepository businessRepository;

    private final BusinessclaimRepository businessClaimRepository;

    public WebsiteService(
            WebsiteRepository websiteRepository,
            ReviewService reviewService,
            BusinessRepository businessRepository,
            BusinessclaimRepository businessClaimRepository) {

        this.websiteRepository = websiteRepository;
        this.reviewService = reviewService;
        this.businessRepository = businessRepository;
        this.businessClaimRepository = businessClaimRepository;
    }

    public Website saveWebsite(Website website) {

        String canonicalDomain =
                normalizeDomain(website.getUrl());

        Website existingWebsite =
                websiteRepository
                        .findByCanonicalDomain(canonicalDomain)
                        .orElse(null);

        if (existingWebsite != null) {
            return existingWebsite;
        }

        website.setCanonicalDomain(canonicalDomain);

        return websiteRepository.save(website);
    }

    public List<WebsiteResponseDTO> getAllWebsites() {

        return websiteRepository
                .findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    // ==============================
    // WEBSITE PROFILE BY ID
    // ==============================

    public WebsiteResponseDTO getWebsiteById(Long id) {

        Website website =
                websiteRepository
                        .findById(id)
                        .orElse(null);

        if (website == null) {
            return null;
        }

        return convertToResponseDTO(website);
    }

    // ==============================
    // WEBSITE PROFILE BY DOMAIN
    // ==============================

    public WebsiteResponseDTO getWebsiteByDomain(
            String domain) {

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

    // ==============================
    // WEBSITE REVIEWS
    // ==============================

    public List<ReviewResponseDTO> getWebsiteReviews(
            Long websiteId) {

        List<Review> reviews =
                reviewService
                        .getReviewsByWebsiteId(websiteId);

        return reviewService
                .convertToResponseDTOList(reviews);
    }

    // ==============================
    // WEBSITE SEARCH
    // ==============================

    public List<WebsiteResponseDTO> searchWebsites(
            String query) {

        List<Website> websites;

        if (query == null
                || query.trim().isEmpty()) {

            websites =
                    websiteRepository.findAll();

        } else {

            String searchQuery =
                    query.trim();

            websites =
                    websiteRepository
                            .findByNameContainingIgnoreCaseOrCanonicalDomainContainingIgnoreCase(
                                    searchQuery,
                                    searchQuery);
        }

        return websites
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    // ==============================
    // RELATED WEBSITES
    // ==============================

    public List<WebsiteResponseDTO> getRelatedWebsites(
            Long websiteId) {

        Website website =
                websiteRepository
                        .findById(websiteId)
                        .orElse(null);

        if (website == null) {
            return List.of();
        }

        String keyword =
                getRelatedKeyword(website);

        if (keyword.isEmpty()) {
            return List.of();
        }

        return websiteRepository
                .findRelatedWebsites(
                        websiteId,
                        keyword)
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    // ==============================
    // GET KEYWORD FOR RELATED WEBSITES
    // ==============================

    private String getRelatedKeyword(
            Website website) {

        if (website.getName() != null
                && !website.getName().trim().isEmpty()) {

            String[] words =
                    website.getName()
                            .trim()
                            .split("\\s+");

            for (String word : words) {

                if (word.length() >= 3) {
                    return word;
                }
            }
        }

        if (website.getDescription() != null
                && !website.getDescription()
                        .trim()
                        .isEmpty()) {

            String[] words =
                    website.getDescription()
                            .trim()
                            .split("\\s+");

            for (String word : words) {

                if (word.length() >= 3) {
                    return word;
                }
            }
        }

        return "";
    }

    // ==============================
    // ADMIN SEO UPDATE
    // ==============================

    public Website updateSeo(
            Long id,
            String seoTitle,
            String seoDescription,
            String canonicalUrl) {

        Website website =
                websiteRepository
                        .findById(id)
                        .orElse(null);

        if (website == null) {
            throw new RuntimeException(
                    "Website not found");
        }

        website.setSeoTitle(seoTitle);
        website.setSeoDescription(seoDescription);
        website.setCanonicalUrl(canonicalUrl);

        return websiteRepository.save(website);
    }

    // ==============================
    // DELETE WEBSITE
    // ==============================

    public void deleteWebsite(Long id) {

        reviewService.deleteReviewsByWebsiteId(id);

        websiteRepository.deleteById(id);
    }

    // ==============================
    // ENTITY TO DTO CONVERSION
    // ==============================

    private WebsiteResponseDTO convertToResponseDTO(
            Website website) {

        // ------------------------------
        // Rating information
        // ------------------------------

        double averageRating =
                reviewService
                        .getAverageRating(website.getId());

        int reviewCount =
                reviewService
                        .getReviewCount(website.getId());

        int fiveStarCount =
                reviewService
                        .getFiveStarCount(website.getId());

        int fourStarCount =
                reviewService
                        .getFourStarCount(website.getId());

        int threeStarCount =
                reviewService
                        .getThreeStarCount(website.getId());

        int twoStarCount =
                reviewService
                        .getTwoStarCount(website.getId());

        int oneStarCount =
                reviewService
                        .getOneStarCount(website.getId());

        // ------------------------------
        // Business claim information
        // ------------------------------

        boolean claimed = false;
        boolean verified = false;

        Business business =
                getBusinessForWebsiteInternal(website);

        if (business != null) {

            BusinessClaim approvedClaim =
                    businessClaimRepository
                            .findByBusinessIdAndStatus(
                                    business.getId(),
                                    "APPROVED")
                            .orElse(null);

            if (approvedClaim != null) {
                claimed = true;
            }

            if ("VERIFIED".equalsIgnoreCase(
                    business.getStatus())) {

                verified = true;
            }
        }

        return new WebsiteResponseDTO(
                website.getId(),
                website.getName(),
                website.getUrl(),
                website.getDescription(),
                website.getCanonicalDomain(),
                website.getSeoTitle(),
                website.getSeoDescription(),
                website.getCanonicalUrl(),
                claimed,
                verified,
                averageRating,
                reviewCount,
                fiveStarCount,
                fourStarCount,
                threeStarCount,
                twoStarCount,
                oneStarCount
        );
    }

    // ==============================
    // FIND BUSINESS FOR WEBSITE
    // ==============================

    private Business getBusinessForWebsiteInternal(
            Website website) {

        String canonicalDomain =
                website.getCanonicalDomain();

        if (canonicalDomain == null
                || canonicalDomain.isBlank()) {

            return null;
        }

        List<Business> businesses =
                businessRepository.findAll();

        for (Business business : businesses) {

            String officialUrl =
                    business.getOfficialUrl();

            if (officialUrl == null
                    || officialUrl.isBlank()) {

                continue;
            }

            String businessDomain;

            try {

                businessDomain =
                        normalizeDomain(officialUrl);

            } catch (Exception e) {

                continue;
            }

            if (canonicalDomain.equals(
                    businessDomain)) {

                return business;
            }
        }

        return null;
    }

    private String normalizeDomain(String url) {

        try {

            String cleanUrl =
                    url.trim();

            if (!cleanUrl.startsWith("http://")
                    && !cleanUrl.startsWith("https://")) {

                cleanUrl = "https://" + cleanUrl;
            }

            URI uri =
                    new URI(cleanUrl);

            String domain =
                    uri.getHost();

            if (domain == null) {
                throw new RuntimeException(
                        "Invalid website URL");
            }

            domain =
                    domain.toLowerCase();

            if (domain.startsWith("www.")) {

                domain =
                        domain.substring(4);
            }

            return domain;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid website URL");
        }
    }
}

