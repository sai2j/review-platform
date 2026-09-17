package com.nit.discovery;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/discovery")
public class DiscoveryController {


private final DiscoveryService discoveryService;

public DiscoveryController(
        DiscoveryService discoveryService) {

    this.discoveryService = discoveryService;
}

@GetMapping("/search")
public List<WebsiteSearchResult> searchWebsite(
        @RequestParam String keyword) {

    return discoveryService.searchWebsites(keyword);
}

// SIMILAR WEBSITES
@GetMapping("/similar")
public List<WebsiteSearchResult> findSimilarWebsites(
        @RequestParam String keyword) {

    return discoveryService.findSimilarWebsites(keyword);
}

// ALTERNATIVE WEBSITES
@GetMapping("/alternatives")
public List<WebsiteSearchResult> findAlternativeWebsites(
        @RequestParam String keyword) {

    return discoveryService.findAlternativeWebsites(keyword);
}


}
