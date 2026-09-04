package com.nit.Website;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/websites")
public class WebsiteController {

	private final WebsiteService websiteService;

	public WebsiteController(WebsiteService websiteService) {
		this.websiteService = websiteService;
	}

	@PostMapping
	public Website createWebsite(@RequestBody Website website) {
		return websiteService.saveWebsite(website);
	}
	
	@GetMapping
	public List<Website> getAllWebsites(){
		return websiteService.getAllWebsites();
	}
	
	@GetMapping("/{id}")
	public Website getwebsiteById(@PathVariable Long id) {	
		return websiteService.getWebsiteById(id);
	}
	
	@DeleteMapping("/{id}")
	public String deleteWebsite(@PathVariable Long id) {
		websiteService.deleteWebsite(id);
		return " website delete sucessfully";	}
}
