package com.nit.business;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/business-claims", "/api/v1/business/claims"})
public class BusinessClaimcontroller {

    private final BusinessClamService businessClaimService;

    public BusinessClaimcontroller(
            BusinessClamService businessClaimService) {

        this.businessClaimService = businessClaimService;
    }

    // Logged-in user can create a business claim
    @PostMapping
    public BusinessClaim createBusinessClaim(
            @Valid @RequestBody BusinessClaim businessClaim) {

        return businessClaimService.saveBusinessClaim(
                businessClaim);
    }

    // Only ADMIN can view all claims
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<BusinessClaim> getAllBusinessClaims() {

        return businessClaimService.getAllBusinessClaims();
    }

    // Only ADMIN can view a specific claim
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public BusinessClaim getBusinessClaimById(
            @PathVariable Long id) {

        return businessClaimService
                .getBusinessClaimById(id);
    }

    // User can view only their own approved claim
    @GetMapping("/user/{userId}/approved")
    public BusinessClaim getApprovedClaimByUserId(
            @PathVariable Long userId) {

        return businessClaimService
                .getApprovedClaimByUserId(userId);
    }

    // Get approved claim for a specific business
    @GetMapping("/business/{businessId}/approved")
    public BusinessClaim getApprovedClaimByBusinessId(
            @PathVariable Long businessId) {

        return businessClaimService
                .getApprovedClaimByBusinessId(businessId);
    }

    // Only ADMIN can approve/reject a claim
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public BusinessClaim updateClaimStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return businessClaimService
                .updateClaimStatus(id, status);
    }

    // Only ADMIN can delete a claim
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteBusinessClaim(
            @PathVariable Long id) {

        businessClaimService
                .deleteBusinessClaim(id);

        return "Business claim deleted successfully";
    }
}