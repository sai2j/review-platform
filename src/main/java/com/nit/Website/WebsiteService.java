package com.nit.Website;

import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Service;

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

    public List<Website> getAllWebsites() {
        return websiteRepository.findAll();
    }

    public Website getWebsiteById(Long id) {
        return websiteRepository.findById(id).orElse(null);
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