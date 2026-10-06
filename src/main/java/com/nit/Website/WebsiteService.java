
package com.nit.Website;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.nit.business.Business;
import com.nit.business.BusinessRepository;
import com.nit.business.BusinessclaimRepository;
import com.nit.business.BusinessClaim;
import com.nit.dto.ReviewResponseDTO;
import com.nit.dto.WebsitePageResponseDTO;
import com.nit.dto.WebsiteResponseDTO;
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
        String canonicalDomain = normalizeDomain(website.getUrl());

        Website existingWebsite = websiteRepository
                .findByCanonicalDomain(canonicalDomain)
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

    public WebsitePageResponseDTO getWebsitesPage(int page, int size) {
        validatePagination(page, size);

        Page<Website> websitePage =
                websiteRepository.findAll(PageRequest.of(page, size));

        return convertToPageResponse(websitePage);
    }

    // NEW: Paginated website search
    public WebsitePageResponseDTO searchWebsitesPage(
            String query, int page, int size) {

        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Website> websitePage;

        if (query == null || query.trim().isEmpty()) {
            websitePage = websiteRepository.findAll(pageable);
        } else {
            String searchQuery = query.trim();

            websitePage = websiteRepository
                    .findByNameContainingIgnoreCaseOrCanonicalDomainContainingIgnoreCase(
                            searchQuery, searchQuery, pageable);
        }

        return convertToPageResponse(websitePage);
    }

    // NEW: Paginated category/country filtering
    public WebsitePageResponseDTO filterWebsitesPage(
            String category, String country, int page, int size) {

        validatePagination(page, size);

        boolean hasCategory =
                category != null && !category.trim().isEmpty();
        boolean hasCountry =
                country != null && !country.trim().isEmpty();

        Pageable pageable = PageRequest.of(page, size);
        Page<Website> websitePage;

        if (hasCategory && hasCountry) {
            websitePage =
                    websiteRepository.findByCategoryIgnoreCaseAndCountryIgnoreCase(
                            category.trim(), country.trim(), pageable);
        } else if (hasCategory) {
            websitePage = websiteRepository.findByCategoryIgnoreCase(
                    category.trim(), pageable);
        } else if (hasCountry) {
            websitePage = websiteRepository.findByCountryIgnoreCase(
                    country.trim(), pageable);
        } else {
            websitePage = websiteRepository.findAll(pageable);
        }

        return convertToPageResponse(websitePage);
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100");
        }
    }

    private WebsitePageResponseDTO convertToPageResponse(
            Page<Website> websitePage) {

        List<WebsiteResponseDTO> content = websitePage.getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();

        return new WebsitePageResponseDTO(
                content,
                websitePage.getNumber(),
                websitePage.getSize(),
                websitePage.getTotalElements(),
                websitePage.getTotalPages(),
                websitePage.isFirst(),
                websitePage.isLast());
    }

    public WebsiteResponseDTO getWebsiteById(Long id) {
        Website website = websiteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Website not found with id: " + id));

        return convertToResponseDTO(website);
    }

    public WebsiteResponseDTO getWebsiteByDomain(String domain) {
        String canonicalDomain = normalizeDomain(domain);

        Website website = websiteRepository
                .findByCanonicalDomain(canonicalDomain)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Website not found for domain: " + canonicalDomain));

        return convertToResponseDTO(website);
    }

    public List<ReviewResponseDTO> getWebsiteReviews(Long websiteId) {
        if (!websiteRepository.existsById(websiteId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Website not found with id: " + websiteId);
        }

        Pageable pageable = PageRequest.of(0, 100);

        return reviewService
                .getApprovedReviewsByWebsiteId(websiteId, pageable)
                .getContent();
    }

    // Existing non-paginated search retained
    public List<WebsiteResponseDTO> searchWebsites(String query) {
        List<Website> websites;

        if (query == null || query.trim().isEmpty()) {
            websites = websiteRepository.findAll();
        } else {
            String searchQuery = query.trim();

            websites = websiteRepository
                    .findByNameContainingIgnoreCaseOrCanonicalDomainContainingIgnoreCase(
                            searchQuery, searchQuery);
        }

        return websites.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    // Existing non-paginated filter retained
    public List<WebsiteResponseDTO> filterWebsites(
            String category, String country) {

        boolean hasCategory =
                category != null && !category.trim().isEmpty();
        boolean hasCountry =
                country != null && !country.trim().isEmpty();

        List<Website> websites;

        if (hasCategory && hasCountry) {
            websites = websiteRepository
                    .findByCategoryIgnoreCaseAndCountryIgnoreCase(
                            category.trim(), country.trim());
        } else if (hasCategory) {
            websites = websiteRepository
                    .findByCategoryIgnoreCase(category.trim());
        } else if (hasCountry) {
            websites = websiteRepository
                    .findByCountryIgnoreCase(country.trim());
        } else {
            websites = websiteRepository.findAll();
        }

        return websites.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public List<WebsiteResponseDTO> getRelatedWebsites(Long websiteId) {
        Website website = websiteRepository.findById(websiteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Website not found with id: " + websiteId));

        String keyword = getRelatedKeyword(website);

        if (keyword.isEmpty()) {
            return List.of();
        }

        return websiteRepository
                .findRelatedWebsites(websiteId, keyword)
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    private String getRelatedKeyword(Website website) {
        if (website.getName() != null
                && !website.getName().trim().isEmpty()) {
            String[] words = website.getName().trim().split("\\s+");

            for (String word : words) {
                if (word.length() >= 3) {
                    return word;
                }
            }
        }

        if (website.getDescription() != null
                && !website.getDescription().trim().isEmpty()) {
            String[] words =
                    website.getDescription().trim().split("\\s+");

            for (String word : words) {
                if (word.length() >= 3) {
                    return word;
                }
            }
        }

        return "";
    }

    public Website updateSeo(
            Long id, String seoTitle,
            String seoDescription, String canonicalUrl) {

        Website website = websiteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Website not found with id: " + id));

        website.setSeoTitle(seoTitle);
        website.setSeoDescription(seoDescription);
        website.setCanonicalUrl(canonicalUrl);

        return websiteRepository.save(website);
    }

    public void deleteWebsite(Long id) {
        Website website = websiteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Website not found with id: " + id));

        reviewService.deleteReviewsByWebsiteId(website.getId());
        websiteRepository.delete(website);
    }

    private WebsiteResponseDTO convertToResponseDTO(Website website) {
        double averageRating =
                reviewService.getAverageRating(website.getId());
        int reviewCount = reviewService.getReviewCount(website.getId());
        int fiveStarCount = reviewService.getFiveStarCount(website.getId());
        int fourStarCount = reviewService.getFourStarCount(website.getId());
        int threeStarCount = reviewService.getThreeStarCount(website.getId());
        int twoStarCount = reviewService.getTwoStarCount(website.getId());
        int oneStarCount = reviewService.getOneStarCount(website.getId());

        boolean claimed = false;
        boolean verified = false;

        Business business = getBusinessForWebsiteInternal(website);

        if (business != null) {
            BusinessClaim approvedClaim = businessClaimRepository
                    .findByBusinessIdAndStatus(business.getId(), "APPROVED")
                    .orElse(null);

            if (approvedClaim != null) {
                claimed = true;
            }

            if ("VERIFIED".equalsIgnoreCase(business.getStatus())) {
                verified = true;
            }
        }

        WebsiteResponseDTO responseDTO = new WebsiteResponseDTO(
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
                oneStarCount);

        responseDTO.setCategory(website.getCategory());
        responseDTO.setCountry(website.getCountry());

        return responseDTO;
    }

    private Business getBusinessForWebsiteInternal(Website website) {
        String canonicalDomain = website.getCanonicalDomain();

        if (canonicalDomain == null || canonicalDomain.isBlank()) {
            return null;
        }

        List<Business> businesses = businessRepository.findAll();

        for (Business business : businesses) {
            String officialUrl = business.getOfficialUrl();

            if (officialUrl == null || officialUrl.isBlank()) {
                continue;
            }

            String businessDomain;

            try {
                businessDomain = normalizeDomain(officialUrl);
            } catch (Exception e) {
                continue;
            }

            if (canonicalDomain.equals(businessDomain)) {
                return business;
            }
        }

        return null;
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

