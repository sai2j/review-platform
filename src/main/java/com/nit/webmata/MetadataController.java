package com.nit.webmata;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metadata")
public class MetadataController {

	private final MetadataService metadataService;

	public MetadataController(MetadataService metadataService) {
		this.metadataService = metadataService;
	}

	@GetMapping("/fetch")
	public WebsiteMetaData fetchMetaData(@RequestParam String url) {
		return metadataService.fetchMetaData(url);
	}

}
