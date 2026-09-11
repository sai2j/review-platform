package com.nit.business;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    // Create Business Response
    @PostMapping
    public BusinessResponse createBusinessResponse(
            @Valid @RequestBody BusinessResponse businessResponse) {

        return businessResponseService
                .saveBusinessResponse(businessResponse);
    }

    // Get All Business Responses
    @GetMapping
    public List<BusinessResponse> getAllBusinessResponses() {

        return businessResponseService
                .getAllBusinessResponses();
    }

    // Get Business Response By ID
    @GetMapping("/{id}")
    public BusinessResponse getBusinessResponseById(
            @PathVariable Long id) {

        return businessResponseService
                .getBusinessResponseById(id);
    }

    // Update Business Response
    @PutMapping("/{id}")
    public BusinessResponse updateBusinessResponse(
            @PathVariable Long id,
            @Valid @RequestBody BusinessResponse businessResponse) {

        return businessResponseService
                .updateBusinessResponse(
                        id,
                        businessResponse);
    }

    // Delete Business Response
    @DeleteMapping("/{id}")
    public String deleteBusinessResponse(
            @PathVariable Long id) {

        businessResponseService
                .deleteBusinessResponse(id);

        return "Business response deleted successfully";
    }
}