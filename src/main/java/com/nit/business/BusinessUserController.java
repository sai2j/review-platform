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
@RequestMapping("/business-users")
public class BusinessUserController {

    private final BusinessUserService businessUserService;
	
    public BusinessUserController(BusinessUserService businessUserService) {
        this.businessUserService = businessUserService;
    }
    @PostMapping
    public BusinessUser createBusinessUser(@RequestBody BusinessUser businessUser) {
        return businessUserService.saveBusinessUser(businessUser);
    }
    @GetMapping
	public List<BusinessUser> getAllBusinessUsers(){
		return businessUserService.getAllBusinessUsers();	
	}
    @GetMapping("/{id}")
	public BusinessUser getBusinessUserByid(@PathVariable Long id) {
		return businessUserService.getBusinessUserById(id);
	}
    @DeleteMapping("/{id}")
    public String deleteBusinessUser(@PathVariable Long id) {
    	businessUserService.deleteBusinessUser(id);
    	return "business user delete sucessfully";
    }
}
