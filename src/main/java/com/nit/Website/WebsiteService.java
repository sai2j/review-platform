package com.nit.Website;

import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class WebsiteService {

    private final WebsiteRepository websiteRepository;

    public WebsiteService(WebsiteRepository websiteRepository) {
        this.websiteRepository = websiteRepository;
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

    public void deleteWebsite(Long id) {
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