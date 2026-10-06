package com.nit.Website;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nit.business.BusinessRepository;
import com.nit.business.BusinessclaimRepository;
import com.nit.review.ReviewService;

@ExtendWith(MockitoExtension.class)
class WebsiteServiceTest {

	@Mock
	private WebsiteRepository websiteRepository;

	@Mock
	private ReviewService reviewService;

	@Mock
	private BusinessRepository businessRepository;

	@Mock
	private BusinessclaimRepository businessClaimRepository;

	private WebsiteService websiteService;

	@BeforeEach
	void setUp() {

		websiteService = new WebsiteService(websiteRepository, reviewService, businessRepository,
				businessClaimRepository);
	}

	@Test
	void shouldNormalizeWebsiteDomain() {

		Website website = new Website();

		website.setName("Test Website");
		website.setUrl("https://www.Example.com");

		when(websiteRepository.findByCanonicalDomain("example.com")).thenReturn(Optional.empty());

		when(websiteRepository.save(website)).thenReturn(website);

		Website result = websiteService.saveWebsite(website);

		assertEquals("example.com", result.getCanonicalDomain());
	}

	@Test
	void shouldNormalizeDomainWithoutProtocol() {

		Website website = new Website();

		website.setName("Test Website");
		website.setUrl("www.Example.com");

		when(websiteRepository.findByCanonicalDomain("example.com")).thenReturn(Optional.empty());

		when(websiteRepository.save(website)).thenReturn(website);

		Website result = websiteService.saveWebsite(website);

		assertEquals("example.com", result.getCanonicalDomain());
	}

	@Test
	void shouldNormalizeHttpDomain() {

		Website website = new Website();

		website.setName("Test Website");
		website.setUrl("http://www.Example.com");

		when(websiteRepository.findByCanonicalDomain("example.com")).thenReturn(Optional.empty());

		when(websiteRepository.save(website)).thenReturn(website);

		Website result = websiteService.saveWebsite(website);

		assertEquals("example.com", result.getCanonicalDomain());
	}
}