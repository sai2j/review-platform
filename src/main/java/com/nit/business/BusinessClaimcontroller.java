package com.nit.business;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/business-claims", "/api/v1/business/claims" })
public class BusinessClaimcontroller {

	private final BusinessClamService businessClaimService;

	public BusinessClaimcontroller(BusinessClamService businessClaimService) {
		this.businessClaimService = businessClaimService;
	}

	@PostMapping
	public BusinessClaim createBusinessClaim(@Valid @RequestBody BusinessClaim businessClaim) {
		return businessClaimService.saveBusinessClaim(businessClaim);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping
	public List<BusinessClaim> getAllBusinessClaims() {
		return businessClaimService.getAllBusinessClaims();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{id}")
	public BusinessClaim getBusinessClaimById(@PathVariable Long id) {
		return businessClaimService.getBusinessClaimById(id);
	}

	@GetMapping("/user/{userId}/approved")
	public BusinessClaim getApprovedClaimByUserId(@PathVariable Long userId) {
		return businessClaimService.getApprovedClaimByUserId(userId);
	}

	@GetMapping("/business/{businessId}/approved")
	public BusinessClaim getApprovedClaimByBusinessId(@PathVariable Long businessId) {
		return businessClaimService.getApprovedClaimByBusinessId(businessId);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{id}/status")
	public BusinessClaim updateClaimStatus(@PathVariable Long id, @RequestParam String status) {
		return businessClaimService.updateClaimStatus(id, status);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public String deleteBusinessClaim(@PathVariable Long id) {
		businessClaimService.deleteBusinessClaim(id);
		return "Business claim deleted successfully";
	}
}