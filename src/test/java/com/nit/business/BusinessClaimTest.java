package com.nit.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.nit.user.User;
import com.nit.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class BusinessClaimTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BusinessRepository businessRepository;

	@Autowired
	private BusinessclaimRepository businessClaimRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private Long testUserId;
	private Long testBusinessId;
	private Long testClaimId;

	@AfterEach
	void cleanup() {

		if (testClaimId != null) {
			businessClaimRepository.deleteById(testClaimId);
		}

		if (testBusinessId != null) {
			businessRepository.deleteById(testBusinessId);
		}

		if (testUserId != null) {
			userRepository.deleteById(testUserId);
		}
	}

	@Test
	void shouldCreateBusinessClaim() throws Exception {

		// Create temporary user
		User user = new User();

		user.setName("Claim Test User");
		user.setEmail("claim-test-" + UUID.randomUUID() + "@example.com");
		user.setPassword(passwordEncoder.encode("TestPassword123!"));
		user.setRole("USER");

		User savedUser = userRepository.save(user);
		testUserId = savedUser.getId();

		// Create temporary business
		Business business = new Business();

		business.setName("Claim Test Business " + UUID.randomUUID());
		business.setDescription("Business created for claim test");
		business.setOfficialUrl("https://claim-test.example.com");
		business.setStatus("PENDING");

		Business savedBusiness = businessRepository.save(business);
		testBusinessId = savedBusiness.getId();

		// Authentication
		Authentication authentication = new UsernamePasswordAuthenticationToken(savedUser.getEmail(), null,
				List.of(new SimpleGrantedAuthority("ROLE_USER")));

		// Create business claim
		String requestBody = """
				{
				    "businessId": %d,
				    "userId": %d
				}
				""".formatted(savedBusiness.getId(), savedUser.getId());

		var result = mockMvc
				.perform(post("/business-claims").with(authentication(authentication))
						.contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().is2xxSuccessful()).andReturn();

		// Find created claim
		Optional<BusinessClaim> savedClaim = businessClaimRepository.findByUserIdAndStatus(savedUser.getId(),
				"PENDING");

		assertTrue(savedClaim.isPresent(), "Business claim should be created");

		testClaimId = savedClaim.get().getId();

		assertEquals(savedBusiness.getId(), savedClaim.get().getBusinessId());

		assertEquals(savedUser.getId(), savedClaim.get().getUserId());

		assertEquals("PENDING", savedClaim.get().getStatus());
	}
}