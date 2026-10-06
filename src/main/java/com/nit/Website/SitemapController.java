package com.nit.Website;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nit.review.ReviewRepository;

@RestController
public class SitemapController {

	private final WebsiteRepository websiteRepository;
	private final ReviewRepository reviewRepository;

	@Value("${app.base-url:http://localhost:8080/review-platform}")
	private String baseUrl;

	public SitemapController(WebsiteRepository websiteRepository, ReviewRepository reviewRepository) {

		this.websiteRepository = websiteRepository;
		this.reviewRepository = reviewRepository;
	}

	@GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
	public String getSitemap() {

		StringBuilder xml = new StringBuilder();

		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

		xml.append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

		xml.append("<sitemap>");

		xml.append("<loc>");

		xml.append(escapeXml(baseUrl + "/website-sitemap.xml"));

		xml.append("</loc>");

		xml.append("</sitemap>");

		xml.append("</sitemapindex>");

		return xml.toString();
	}

	@GetMapping(value = "/website-sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
	public String getWebsiteSitemap() {

		List<Website> websites = websiteRepository.findAll();

		StringBuilder xml = new StringBuilder();

		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

		xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

		// Home page
		xml.append("<url>");

		xml.append("<loc>");

		xml.append(escapeXml(baseUrl + "/index.html"));

		xml.append("</loc>");

		xml.append("</url>");

		// Website pages
		for (Website website : websites) {

			if (website == null || website.getId() == null) {
				continue;
			}

			int approvedReviewCount = reviewRepository.countApprovedReviews(website.getId()).intValue();

			boolean hasDescription = website.getDescription() != null && !website.getDescription().trim().isEmpty();

			boolean indexable = approvedReviewCount > 0 || hasDescription;

			if (!indexable) {
				continue;
			}

			String websitePageUrl = baseUrl + "/website/" + website.getId();

			xml.append("<url>");

			xml.append("<loc>");

			xml.append(escapeXml(websitePageUrl));

			xml.append("</loc>");

			xml.append("</url>");
		}

		xml.append("</urlset>");

		return xml.toString();
	}

	private String escapeXml(String value) {

		if (value == null) {
			return "";
		}

		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&apos;");
	}
}