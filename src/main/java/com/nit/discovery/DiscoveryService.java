package com.nit.discovery;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DiscoveryService {

	private final RestTemplate restTemplate;

	@Value("${tinyfish.api-key:TEST_KEY}")
	private String apiKey;

	public DiscoveryService() {
		this.restTemplate = new RestTemplate();
	}

// =========================================================
// SEARCH WEBSITE
// =========================================================

	public List<WebsiteSearchResult> searchWebsites(String keyword) {

		List<WebsiteSearchResult> results = new ArrayList<>();

		if (keyword == null || keyword.trim().isEmpty()) {
			return results;
		}

		String url = UriComponentsBuilder.fromUriString("https://api.search.tinyfish.ai").queryParam("query", keyword)
				.toUriString();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("X-API-Key", apiKey);

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

		Map body = response.getBody();

		if (body == null) {
			return results;
		}

		List<Map<String, Object>> searchResults = (List<Map<String, Object>>) body.get("results");

		if (searchResults == null || searchResults.isEmpty()) {
			return results;
		}

		String searchKeyword = keyword.toLowerCase().trim().replaceAll("\\s+", "");

		WebsiteSearchResult bestResult = null;
		int bestScore = 0;

		for (Map<String, Object> item : searchResults) {

			String title = item.get("title") != null ? item.get("title").toString() : "";

			String resultUrl = item.get("url") != null ? item.get("url").toString() : "";

			String description = item.get("snippet") != null ? item.get("snippet").toString() : "";

			if (resultUrl.isBlank()) {
				continue;
			}

			if (!isActualWebsite(resultUrl)) {
				continue;
			}

			String domain = getDomain(resultUrl);

			if (domain == null) {
				continue;
			}

			String cleanDomain = domain.replace(".", "").replace("-", "").toLowerCase();

			String cleanTitle = title.toLowerCase().replaceAll("\\s+", "");

			int score = 0;

			if (cleanDomain.equals(searchKeyword)) {
				score += 100;
			} else if (cleanDomain.contains(searchKeyword)) {
				score += 50;
			}

			if (cleanTitle.equals(searchKeyword)) {
				score += 40;
			} else if (cleanTitle.contains(searchKeyword)) {
				score += 20;
			}

			if (resultUrl.toLowerCase().contains(keyword.toLowerCase())) {
				score += 10;
			}

			if (score == 0) {
				continue;
			}

			if (bestResult == null || score > bestScore) {

				bestScore = score;

				bestResult = new WebsiteSearchResult(title, resultUrl, description);
			}
		}

		if (bestResult != null) {
			results.add(bestResult);
		}

		return results;
	}

// =========================================================
// SIMILAR WEBSITES
// =========================================================

	public List<WebsiteSearchResult> findSimilarWebsites(String keyword) {

		List<WebsiteSearchResult> results = new ArrayList<>();

		if (keyword == null || keyword.trim().isEmpty()) {

			return results;
		}

		String searchQuery = "websites similar to " + keyword.trim();

		String url = UriComponentsBuilder.fromUriString("https://api.search.tinyfish.ai")
				.queryParam("query", searchQuery).toUriString();

		HttpHeaders headers = new HttpHeaders();

		headers.set("Accept", "application/json");

		headers.set("X-API-Key", apiKey);

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

		Map body = response.getBody();

		if (body == null) {
			return results;
		}

		List<Map<String, Object>> searchResults = (List<Map<String, Object>>) body.get("results");

		if (searchResults == null || searchResults.isEmpty()) {

			return results;
		}

		String keywordLower = keyword.trim().toLowerCase();

		for (Map<String, Object> item : searchResults) {

			String title = item.get("title") != null ? item.get("title").toString() : "";

			String resultUrl = item.get("url") != null ? item.get("url").toString() : "";

			String description = item.get("snippet") != null ? item.get("snippet").toString() : "";

			if (resultUrl.isBlank()) {
				continue;
			}

			if (!isActualWebsite(resultUrl)) {
				continue;
			}

			String domain = getDomain(resultUrl);

			if (domain == null) {
				continue;
			}

			if (domain.equals(keywordLower) || domain.contains(keywordLower.replace(" ", ""))) {

				continue;
			}

			boolean duplicate = results.stream().anyMatch(result -> getDomain(result.getUrl()) != null
					&& getDomain(result.getUrl()).equalsIgnoreCase(domain));

			if (duplicate) {
				continue;
			}

			results.add(new WebsiteSearchResult(title, resultUrl, description));

			if (results.size() >= 5) {
				break;
			}
		}

		return results;
	}

// =========================================================
// ALTERNATIVE WEBSITES
// =========================================================

	public List<WebsiteSearchResult> findAlternativeWebsites(String keyword) {

		List<WebsiteSearchResult> results = new ArrayList<>();

		if (keyword == null || keyword.trim().isEmpty()) {

			return results;
		}

		String searchQuery = "best alternatives to " + keyword.trim() + " official websites";

		String url = UriComponentsBuilder.fromUriString("https://api.search.tinyfish.ai")
				.queryParam("query", searchQuery).toUriString();

		HttpHeaders headers = new HttpHeaders();

		headers.set("Accept", "application/json");

		headers.set("X-API-Key", apiKey);

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

		Map body = response.getBody();

		if (body == null) {
			return results;
		}

		List<Map<String, Object>> searchResults = (List<Map<String, Object>>) body.get("results");

		if (searchResults == null || searchResults.isEmpty()) {

			return results;
		}

		String keywordLower = keyword.trim().toLowerCase();

		String cleanKeyword = keywordLower.replaceAll("[^a-z0-9]", "");

		for (Map<String, Object> item : searchResults) {

			String title = item.get("title") != null ? item.get("title").toString() : "";

			String resultUrl = item.get("url") != null ? item.get("url").toString() : "";

			String description = item.get("snippet") != null ? item.get("snippet").toString() : "";

			if (resultUrl.isBlank()) {
				continue;
			}

			if (!isActualWebsite(resultUrl)) {
				continue;
			}

			String domain = getDomain(resultUrl);

			if (domain == null) {
				continue;
			}

			String cleanDomain = domain.replace(".", "").replace("-", "").toLowerCase();

			// Original website ko exclude karo
			if (cleanDomain.equals(cleanKeyword) || cleanDomain.contains(cleanKeyword)) {

				continue;
			}

			// Duplicate domain ko exclude karo
			boolean duplicate = results.stream().anyMatch(result -> getDomain(result.getUrl()) != null
					&& getDomain(result.getUrl()).equalsIgnoreCase(domain));

			if (duplicate) {
				continue;
			}

			// Article/list pages ko avoid karne ke liye
			// title/description mein alternative context check karo
			String combinedText = (title + " " + description).toLowerCase();

			if (!combinedText.contains("alternative") && !combinedText.contains("music")
					&& !combinedText.contains("streaming") && !combinedText.contains("service")) {

				continue;
			}

			results.add(new WebsiteSearchResult(title, resultUrl, description));

			if (results.size() >= 5) {
				break;
			}
		}

		return results;
	}

// =========================================================
// CHECK ACTUAL WEBSITE
// =========================================================

	private boolean isActualWebsite(String url) {

		try {

			URI uri = URI.create(url);

			String host = uri.getHost();

			if (host == null) {
				return false;
			}

			String lowerHost = host.toLowerCase();

			String lowerUrl = url.toLowerCase();

			String[] unwantedDomains = {

					"wikipedia.org", "youtube.com", "facebook.com", "instagram.com", "linkedin.com", "reddit.com",
					"quora.com", "twitter.com", "x.com", "pinterest.com", "apps.apple.com", "play.google.com",
					"apps.microsoft.com", "microsoft.com/store"

			};

			for (String unwanted : unwantedDomains) {

				if (lowerHost.equals(unwanted) || lowerHost.endsWith("." + unwanted) || lowerUrl.contains(unwanted)) {

					return false;
				}
			}

			String path = uri.getPath();

			if (path != null && !path.equals("/") && !path.isEmpty()) {

				String lowerPath = path.toLowerCase();

				String[] unwantedPaths = {

						"/wiki/", "/article/", "/articles/", "/news/", "/blog/", "/blogs/", "/search", "/tag/",
						"/category/", "/topics/", "/stories/", "/post/"

				};

				for (String unwantedPath : unwantedPaths) {

					if (lowerPath.contains(unwantedPath)) {

						return false;
					}
				}
			}

			if (lowerHost.contains("apps.apple.com")) {

				return false;
			}

			if (lowerHost.contains("play.google.com")) {

				return false;
			}

			return true;

		} catch (Exception e) {

			return false;
		}
	}

// =========================================================
// GET DOMAIN
// =========================================================

	private String getDomain(String url) {

		try {

			URI uri = URI.create(url);

			String host = uri.getHost();

			if (host == null) {
				return null;
			}

			host = host.toLowerCase();

			if (host.startsWith("www.")) {
				host = host.substring(4);
			}

			return host;

		} catch (Exception e) {

			return null;
		}
	}

}
