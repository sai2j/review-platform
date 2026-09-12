package com.nit.business;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/business-responses")
public class BusinessResponseController {

    private final BusinessResponseService businessResponseService;

    public BusinessResponseController(
            BusinessResponseService businessResponseService) {

        this.businessResponseService = businessResponseService;
    }

    // Only verified business user can create response
    @PreAuthorize("@businessResponseService.canAccessBusiness(#businessResponse.businessId)")
    @PostMapping
    public BusinessResponse createBusinessResponse(
            @Valid @RequestBody BusinessResponse businessResponse) {

        return businessResponseService
                .saveBusinessResponse(businessResponse);
    }

    // Only ADMIN can view all responses
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<BusinessResponse> getAllBusinessResponses() {

        return businessResponseService
                .getAllBusinessResponses();
    }

    // Only ADMIN can view response by ID
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public BusinessResponse getBusinessResponseById(
            @PathVariable Long id) {

        return businessResponseService
                .getBusinessResponseById(id);
    }

    // Only verified business user can update response
    @PreAuthorize("@businessResponseService.canAccessResponse(#id)")
    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public BusinessResponse updateBusinessResponse(
            @PathVariable Long id,
            @Valid @RequestBody BusinessResponse businessResponse) {

        return businessResponseService
                .updateBusinessResponse(
                        id,
                        businessResponse);
    }

    // Only verified business user can delete response
    @PreAuthorize("@businessResponseService.canAccessResponse(#id)")
    @DeleteMapping("/{id}")
    public String deleteBusinessResponse(
            @PathVariable Long id) {

        businessResponseService
                .deleteBusinessResponse(id);

        return "Business response deleted successfully";
    }
}