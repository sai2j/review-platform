package com.nit.business;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @DeleteMapping("/{id}")
    public String deleteBusinessClaim(@PathVariable Long id) {
        businessClaimService.deleteBusinessClaim(id);
        return "Business claim deleted successfully";
    }

}
