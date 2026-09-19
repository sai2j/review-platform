package com.nit.business;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/businesses")
public class businessController {

    private final BusinessService businessService;
    private final BusinessEmailVerificationService emailVerificationService;

    public businessController(
            BusinessService businessService,
            BusinessEmailVerificationService emailVerificationService) {

        this.businessService = businessService;
        this.emailVerificationService = emailVerificationService;
    }

    // Only authenticated users can create a business
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public Business createBusiness(
            @Valid @RequestBody Business business) {

        return businessService.saveBusiness(business);
    }

    // Public business listing
    @GetMapping
    public List<Business> getAllBusinesses() {

        return businessService.getAllBusinesses();
    }

    // Public business details
    @GetMapping("/{id}")
    public Business getBusinessById(
            @PathVariable Long id) {

        return businessService.getBusinessById(id);
    }

    // Business owner can edit only their approved claimed business
    @PreAuthorize("@businessService.canAccessBusiness(#id)")
    @PutMapping("/{id}")
    public Business updateBusiness(
            @PathVariable Long id,
            @Valid @RequestBody Business business) {

        return businessService.updateBusiness(id, business);
    }

    // Only ADMIN can verify business
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/verify")
    public Business verifyBusiness(
            @PathVariable Long id) {

        return businessService.verifyBusiness(id);
    }

    // =========================================================
    // BUSINESS EMAIL VERIFICATION
    // =========================================================

    // Only approved business owner can request email verification
    @PreAuthorize("@businessService.canAccessBusiness(#id)")
    @PostMapping("/{id}/verify-email")
    public String sendBusinessEmailVerification(
            @PathVariable Long id,
            @RequestParam String email) {

        Business business =
                businessService.createEmailVerificationToken(
                        id, email);

        emailVerificationService
                .sendVerificationEmail(business);

        return "Verification email sent successfully";
    }

    // Verification token endpoint remains accessible for email-link verification
    @GetMapping("/{id}/verify-email")
    public String verifyBusinessEmail(
            @PathVariable Long id,
            @RequestParam String token) {

        businessService.verifyBusinessEmail(
                id, token);

        return "Business email verified successfully";
    }

    // =========================================================
    // META TAG VERIFICATION
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/generate-meta-token")
    public String generateMetaVerificationToken(
            @PathVariable Long id) {

        Business business =
                businessService.createMetaVerificationToken(id);

        return "Meta verification token generated: "
                + business.getMetaVerificationToken();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/verify-meta")
    public Business verifyBusinessMetaTag(
            @PathVariable Long id) {

        return businessService.verifyBusinessMetaTag(id);
    }

    // =========================================================
    // DNS VERIFICATION
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/generate-dns-token")
    public String generateDnsVerificationToken(
            @PathVariable Long id) {

        Business business =
                businessService.createDnsVerificationToken(id);

        return "DNS verification token generated: "
                + business.getDnsVerificationToken();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/verify-dns")
    public Business verifyBusinessDns(
            @PathVariable Long id) {

        return businessService.verifyBusinessDns(id);
    }

    // =========================================================
    // OTHER BUSINESS METHODS
    // =========================================================

    @GetMapping("/website/{websiteId}")
    public Business getBusinessForWebsite(
            @PathVariable Long websiteId) {

        return businessService
                .getBusinessForWebsite(websiteId);
    }

    // Only ADMIN can delete a business
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteBusiness(
            @PathVariable Long id) {

        businessService.deleteBusiness(id);

        return "Business deleted successfully";
    }
}