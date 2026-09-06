package com.nit.business;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business-claims")
public class BusinessClaimcontroller {

    private final BusinessClamService businessClaimService;

    public BusinessClaimcontroller(BusinessClamService businessClaimService) {
        this.businessClaimService = businessClaimService;
    }
    @PostMapping
    public BusinessClaim createBusinessClaim(@RequestBody BusinessClaim businessClaim) {
        return businessClaimService.saveBusinessClaim(businessClaim);
    }
    @GetMapping
    public List<BusinessClaim> getAllBusinessClaims() {
        return businessClaimService.getAllBusinessClaims();
    }
    @GetMapping("/{id}")
    public BusinessClaim getBusinessClaimById(@PathVariable Long id) {
        return businessClaimService.getBusinessClaimById(id);
    }
    @PutMapping("/{id}/status")
    public BusinessClaim updateClaimStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return businessClaimService.updateClaimStatus(id, status);
    }
    @DeleteMapping("/{id}")
    public String deleteBusinessClaim(@PathVariable Long id) {
        businessClaimService.deleteBusinessClaim(id);
        return "Business claim deleted successfully";
    }
}