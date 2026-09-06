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
@RequestMapping("/businesses")
public class businessController {

	private final BusinessService businessService;

	public businessController(BusinessService businessService) {
		super();
		this.businessService = businessService;
	}
	@PostMapping
	public Business createBusiness(@RequestBody Business business) {
		return businessService.saveBusiness(business);
	}
	@GetMapping
	public List<Business> getAllBusinesses() {
		return businessService.getAllBusinesses();
	}
	@GetMapping("/{id}")
	public Business getBusinessById(@PathVariable Long id) {
		return businessService.getBusinessById(id);
	}
	@DeleteMapping("/{id}")
	public String deleteBusiness(@PathVariable Long id) {
		businessService.deleteBusiness(id);
		return "Business deleted successfully";
	}
}
