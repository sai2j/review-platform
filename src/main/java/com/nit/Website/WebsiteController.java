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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/websites")
public class WebsiteController {

    private final WebsiteService websiteService;

    public WebsiteController(WebsiteService websiteService) {
        this.websiteService = websiteService;
    }

    @PostMapping
    public Website createWebsite(@Valid @RequestBody Website website) {
        return websiteService.saveWebsite(website);
    }

    @GetMapping
    public List<Website> getAllWebsites() {
        return websiteService.getAllWebsites();
    }

    @GetMapping("/{id}")
    public Website getwebsiteById(@PathVariable Long id) {
        return websiteService.getWebsiteById(id);
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