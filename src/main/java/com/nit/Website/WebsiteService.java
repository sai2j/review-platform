package com.nit.Website;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class WebsiteService {

	private final WebsiteRepository websiteRepository;

	public WebsiteService(WebsiteRepository websiteRepository) {
		this.websiteRepository = websiteRepository;
	}
	public Website saveWebsite(Website website) {
		return websiteRepository.save(website);
	}
	public List<Website> getAllWebsites() {
		return websiteRepository.findAll();
	}
	public Website getWebsiteById(Long id) {
		return websiteRepository.findById(id).orElse(null);
	}
	public void deleteWebsite(Long id) {
		websiteRepository.deleteById(id);
	}
}
