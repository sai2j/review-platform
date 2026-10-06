
package com.nit.business;

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

import com.nit.dto.BusinessResponseDTO;

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

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public BusinessResponseDTO createBusiness(
            @Valid @RequestBody Business business) {

        return toBusinessResponseDTO(
                businessService.saveBusiness(business)
        );
    }

    @GetMapping
    public List<BusinessResponseDTO> getAllBusinesses() {

        return businessService.getAllBusinesses()
                .stream()
                .map(this::toBusinessResponseDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public BusinessResponseDTO getBusinessById(
            @PathVariable Long id) {

        return toBusinessResponseDTO(
                businessService.getBusinessById(id)
        );
    }

    @PreAuthorize("@businessService.canAccessBusiness(#id)")
    @PutMapping("/{id}")
    public BusinessResponseDTO updateBusiness(
            @PathVariable Long id,
            @Valid @RequestBody Business business) {

        return toBusinessResponseDTO(
                businessService.updateBusiness(id, business)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/verify")
    public BusinessResponseDTO verifyBusiness(
            @PathVariable Long id) {

        return toBusinessResponseDTO(
                businessService.verifyBusiness(id)
        );
    }

    @PreAuthorize("@businessService.canAccessBusiness(#id)")
    @PostMapping("/{id}/verify-email")
    public String sendBusinessEmailVerification(
            @PathVariable Long id,
            @RequestParam String email) {

        Business business =
                businessService.createEmailVerificationToken(id, email);

        emailVerificationService.sendVerificationEmail(business);

        return "Verification email sent successfully";
    }

    @GetMapping("/{id}/verify-email")
    public String verifyBusinessEmail(
            @PathVariable Long id,
            @RequestParam String token) {

        businessService.verifyBusinessEmail(id, token);

        return "Business email verified successfully";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/generate-meta-token")
    public String generateMetaVerificationToken(
            @PathVariable Long id) {

        businessService.createMetaVerificationToken(id);

        return "Meta verification token generated. Retrieve it through the authorized verification workflow.";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/verify-meta")
    public BusinessResponseDTO verifyBusinessMetaTag(
            @PathVariable Long id) {

        return toBusinessResponseDTO(
                businessService.verifyBusinessMetaTag(id)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/generate-dns-token")
    public String generateDnsVerificationToken(
            @PathVariable Long id) {

        businessService.createDnsVerificationToken(id);

        return "DNS verification token generated. Retrieve it through the authorized verification workflow.";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/verify-dns")
    public BusinessResponseDTO verifyBusinessDns(
            @PathVariable Long id) {

        return toBusinessResponseDTO(
                businessService.verifyBusinessDns(id)
        );
    }

    @GetMapping("/website/{websiteId}")
    public BusinessResponseDTO getBusinessForWebsite(
            @PathVariable Long websiteId) {

        return toBusinessResponseDTO(
                businessService.getBusinessForWebsite(websiteId)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteBusiness(@PathVariable Long id) {

        businessService.deleteBusiness(id);

        return "Business deleted successfully";
    }

    private BusinessResponseDTO toBusinessResponseDTO(Business business) {
        if (business == null) {
            return null;
        }

        return new BusinessResponseDTO(
                business.getId(),
                business.getName(),
                business.getDescription(),
                business.getOfficialUrl(),
                business.getStatus(),
                business.getBusinessEmail(),
                business.isEmailVerified(),
                business.isMetaVerified(),
                business.isDnsVerified()
        );
    }
}