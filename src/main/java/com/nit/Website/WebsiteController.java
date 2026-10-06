
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

import com.nit.dto.ReviewResponseDTO;
import com.nit.dto.WebsitePageResponseDTO;
import com.nit.dto.WebsiteResponseDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/websites", "/api/v1/websites" })
public class WebsiteController {

    private final WebsiteService websiteService;

    public WebsiteController(WebsiteService websiteService) {
        this.websiteService = websiteService;
    }

    @PostMapping
    public WebsiteResponseDTO createWebsite(
            @Valid @RequestBody Website website) {
        Website savedWebsite = websiteService.saveWebsite(website);
        return websiteService.getWebsiteById(savedWebsite.getId());
    }

    @GetMapping
    public List<WebsiteResponseDTO> getAllWebsites() {
        return websiteService.getAllWebsites();
    }

    @GetMapping("/page")
    public WebsitePageResponseDTO getWebsitesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return websiteService.getWebsitesPage(page, size);
    }

    // Existing search endpoint preserved
    @GetMapping("/search")
    public List<WebsiteResponseDTO> searchWebsites(
            @RequestParam(required = false) String q) {
        return websiteService.searchWebsites(q);
    }

    // NEW: Paginated search endpoint
    @GetMapping("/search/page")
    public WebsitePageResponseDTO searchWebsitesPage(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return websiteService.searchWebsitesPage(q, page, size);
    }

    // Existing filter endpoint preserved
    @GetMapping("/filter")
    public List<WebsiteResponseDTO> filterWebsites(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String country) {
        return websiteService.filterWebsites(category, country);
    }

    // NEW: Paginated filter endpoint
    @GetMapping("/filter/page")
    public WebsitePageResponseDTO filterWebsitesPage(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return websiteService.filterWebsitesPage(
                category, country, page, size);
    }

    @GetMapping("/domain/{domain}")
    public WebsiteResponseDTO getWebsiteByDomain(
            @PathVariable String domain) {
        return websiteService.getWebsiteByDomain(domain);
    }

    @GetMapping("/{id}/related")
    public List<WebsiteResponseDTO> getRelatedWebsites(
            @PathVariable Long id) {
        return websiteService.getRelatedWebsites(id);
    }

    @GetMapping("/{id}/reviews")
    public List<ReviewResponseDTO> getWebsiteReviews(
            @PathVariable Long id) {
        return websiteService.getWebsiteReviews(id);
    }

    @GetMapping("/{id}")
    public WebsiteResponseDTO getWebsiteById(
            @PathVariable Long id) {
        return websiteService.getWebsiteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/seo")
    public WebsiteResponseDTO updateSeo(
            @PathVariable Long id,
            @RequestParam(required = false) String seoTitle,
            @RequestParam(required = false) String seoDescription,
            @RequestParam(required = false) String canonicalUrl) {

        Website updatedWebsite = websiteService.updateSeo(
                id, seoTitle, seoDescription, canonicalUrl);

        return websiteService.getWebsiteById(updatedWebsite.getId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteWebsite(@PathVariable Long id) {
        websiteService.deleteWebsite(id);
    }
}
