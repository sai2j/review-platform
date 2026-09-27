package com.nit.business;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/business-responses", "/api/v1/business" })
public class BusinessResponseController {

	private final BusinessResponseService businessResponseService;

	public BusinessResponseController(BusinessResponseService businessResponseService) {
		this.businessResponseService = businessResponseService;
	}

	@PreAuthorize("@businessResponseService.canAccessBusiness(#businessResponse.businessId)")
	@PostMapping
	public BusinessResponse createBusinessResponse(
			@Valid @RequestBody BusinessResponse businessResponse) {

		return businessResponseService.saveBusinessResponse(businessResponse);
	}

	@PreAuthorize("@businessResponseService.canAccessBusiness(#businessResponse.businessId)")
	@PostMapping("/reviews/{id}/responses")
	public BusinessResponse createBusinessResponseForReview(
			@PathVariable Long id,
			@Valid @RequestBody BusinessResponse businessResponse) {

		businessResponse.setReviewId(id);

		return businessResponseService.saveBusinessResponse(businessResponse);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping
	public List<BusinessResponse> getAllBusinessResponses() {
		return businessResponseService.getAllBusinessResponses();
	}

	@PreAuthorize("@businessResponseService.canAccessBusiness(#businessId)")
	@GetMapping("/business/{businessId}")
	public List<BusinessResponse> getBusinessResponses(
			@PathVariable Long businessId) {

		return businessResponseService.getBusinessResponses(businessId);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{id}")
	public BusinessResponse getBusinessResponseById(@PathVariable Long id) {
		return businessResponseService.getBusinessResponseById(id);
	}

	@PreAuthorize("@businessResponseService.canAccessResponse(#id)")
	@PutMapping("/{id}")
	public BusinessResponse updateBusinessResponse(
			@PathVariable Long id,
			@Valid @RequestBody BusinessResponse businessResponse) {

		return businessResponseService.updateBusinessResponse(id, businessResponse);
	}

	@PreAuthorize("@businessResponseService.canAccessResponse(#id)")
	@DeleteMapping("/{id}")
	public String deleteBusinessResponse(@PathVariable Long id) {

		businessResponseService.deleteBusinessResponse(id);

		return "Business response deleted successfully";
	}
}