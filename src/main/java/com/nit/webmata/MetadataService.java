package com.nit.webmata;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MetadataService {

	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${tinyfish.api-key:TEST_KEY}")
	private String apiKey;

	public WebsiteMetaData fetchMetaData(String url) {
		String apiUrl = "https://api.fetch.tinyfish.ai";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.set("X-API-Key", apiKey);

		Map<String, Object> requestBody = Map.of("urls", List.of(url));

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

		Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);

		if (response == null) {
			return new WebsiteMetaData();
		}

		List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

		if (results == null || results.isEmpty()) {
			return new WebsiteMetaData();
		}

		Map<String, Object> result = results.get(0);

		String title = result.get("title") != null ? result.get("title").toString() : "";
		String resultUrl = result.get("url") != null ? result.get("url").toString() : url;
		String description = result.get("description") != null ? result.get("description").toString() : "";

		return new WebsiteMetaData(title, resultUrl, description);
	}

}
