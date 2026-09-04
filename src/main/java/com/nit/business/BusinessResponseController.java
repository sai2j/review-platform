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
@RequestMapping("/business-responses")
public class BusinessResponseController {

	private final BusinessResponseService businessResponseService;

    public BusinessResponseController(BusinessResponseService businessResponseService) {
        this.businessResponseService = businessResponseService;
    }

    @PostMapping
    public BusinessResponse createBusinessResponse(
            @RequestBody BusinessResponse businessResponse) {
        return businessResponseService.saveBusinessResponse(businessResponse);
    }

    @GetMapping
    public List<BusinessResponse> getAllBusinessResponses() {
        return businessResponseService.getAllBusinessResponses();
    }

    @GetMapping("/{id}")
    public BusinessResponse getBusinessResponseById(@PathVariable Long id) {
        return businessResponseService.getBusinessResponseById(id);
    }

    @DeleteMapping("/{id}")
    public String deleteBusinessResponse(@PathVariable Long id) {
        businessResponseService.deleteBusinessResponse(id);
        return "Business response deleted successfully";
    }
}
