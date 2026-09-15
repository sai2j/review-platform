package com.nit.business;

import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nit.Website.Website;
import com.nit.Website.WebsiteRepository;
import com.nit.Website.WebsiteService;
import com.nit.review.Review;
import com.nit.review.ReviewRepository;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final WebsiteRepository websiteRepository;
    private final WebsiteService websiteService;
    private final ReviewRepository reviewRepository;

    public BusinessService(
            BusinessRepository businessRepository,
            WebsiteRepository websiteRepository,
            WebsiteService websiteService,
            ReviewRepository reviewRepository) {

        this.businessRepository = businessRepository;
        this.websiteRepository = websiteRepository;
        this.websiteService = websiteService;
        this.reviewRepository = reviewRepository;
    }

    public Business saveBusiness(Business business) {
        return businessRepository.save(business);
    }

    public List<Business> getAllBusinesses() {
        return businessRepository.findAll();
    }

    public Business getBusinessById(Long id) {
        return businessRepository.findById(id).orElse(null);
    }

    public Business updateBusiness(Long id, Business business) {

        Business existingBusiness =
                businessRepository.findById(id).orElse(null);

        if (existingBusiness == null) {
            return null;
        }

        existingBusiness.setName(business.getName());
        existingBusiness.setOfficialUrl(business.getOfficialUrl());
        existingBusiness.setDescription(business.getDescription());
        existingBusiness.setStatus(business.getStatus());

        return businessRepository.save(existingBusiness);
    }

    public void deleteBusiness(Long id) {
        businessRepository.deleteById(id);
    }

    public Website getWebsiteForBusiness(Long businessId) {

        Business business = getBusinessById(businessId);

        if (business == null) {
            return null;
        }

        String officialUrl = business.getOfficialUrl();

        if (officialUrl == null || officialUrl.isBlank()) {
            return null;
        }

        String canonicalDomain = normalizeDomain(officialUrl);

        return websiteRepository
                .findByCanonicalDomain(canonicalDomain)
                .orElse(null);
    }

    public List<Review> getReviewsForBusiness(Long businessId) {

        Website website = getWebsiteForBusiness(businessId);

        if (website == null) {
            return List.of();
        }

        return reviewRepository.findByWebsiteId(website.getId());
    }

    // Website → Business mapping
    public Business getBusinessForWebsite(Long websiteId) {

        Website website =
                websiteRepository.findById(websiteId).orElse(null);

        if (website == null) {
            return null;
        }

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

            String businessDomain =
                    normalizeDomain(officialUrl);

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
                throw new RuntimeException(
                        "Invalid website URL");
            }

            domain = domain.toLowerCase();

            if (domain.startsWith("www.")) {
                domain = domain.substring(4);
            }

            return domain;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid website URL");
        }
    }
}