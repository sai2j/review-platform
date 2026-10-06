package com.nit.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.nit.Website.Website;
import com.nit.Website.WebsiteRepository;
import com.nit.review.Review;
import com.nit.review.ReviewRepository;
import com.nit.user.User;
import com.nit.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class BusinessResponseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private BusinessclaimRepository businessClaimRepository;

    @Autowired
    private BusinessResponseRepository businessResponseRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private WebsiteRepository websiteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long testUserId;
    private Long testBusinessId;
    private Long testClaimId;
    private Long testWebsiteId;
    private Long testReviewId;
    private Long testResponseId;

    @AfterEach
    void cleanup() {

        if (testResponseId != null) {
            businessResponseRepository.deleteById(testResponseId);
        }

        if (testClaimId != null) {
            businessClaimRepository.deleteById(testClaimId);
        }

        if (testReviewId != null) {
            reviewRepository.deleteById(testReviewId);
        }

        if (testWebsiteId != null) {
            websiteRepository.deleteById(testWebsiteId);
        }

        if (testBusinessId != null) {
            businessRepository.deleteById(testBusinessId);
        }

        if (testUserId != null) {
            userRepository.deleteById(testUserId);
        }
    }

    @Test
    void shouldCreateBusinessResponseForApprovedBusiness() throws Exception {

        // ==============================
        // CREATE USER
        // ==============================

        User user = new User();

        user.setName("Business Response Test User");
        user.setEmail(
                "business-response-test-"
                        + UUID.randomUUID()
                        + "@example.com"
        );
        user.setPassword(
                passwordEncoder.encode("TestPassword123!")
        );
        user.setRole("USER");
        user.setStatus("ACTIVE");

        User savedUser = userRepository.save(user);

        testUserId = savedUser.getId();

        // ==============================
        // CREATE VERIFIED BUSINESS
        // ==============================

        Business business = new Business();

        business.setName(
                "Business Response Test Business "
                        + UUID.randomUUID()
        );

        business.setDescription(
                "Business created for response test"
        );

        business.setOfficialUrl(
                "https://business-response-test-"
                        + UUID.randomUUID()
                        + ".example.com"
        );

        business.setStatus("VERIFIED");

        Business savedBusiness =
                businessRepository.save(business);

        testBusinessId = savedBusiness.getId();

        // ==============================
        // CREATE APPROVED BUSINESS CLAIM
        // ==============================

        BusinessClaim claim =
                new BusinessClaim(
                        savedBusiness.getId(),
                        savedUser.getId(),
                        "APPROVED"
                );

        BusinessClaim savedClaim =
                businessClaimRepository.save(claim);

        testClaimId = savedClaim.getId();

        // ==============================
        // CREATE WEBSITE
        // ==============================

        Website website = new Website(
                "Business Response Test Website",
                "https://business-response-test-"
                        + UUID.randomUUID()
                        + ".example.com",
                "Website created for response test"
        );

        website.setCanonicalDomain(
                "business-response-test-"
                        + UUID.randomUUID()
                        + ".example.com"
        );

        Website savedWebsite =
                websiteRepository.save(website);

        testWebsiteId = savedWebsite.getId();

        // ==============================
        // CREATE REVIEW
        // ==============================

        Review review = new Review(
                5,
                "Excellent business experience.",
                savedUser.getId(),
                savedWebsite.getId()
        );

        review.setStatus("APPROVED");
        review.setVerificationStatus("UNVERIFIED");

        Review savedReview =
                reviewRepository.save(review);

        testReviewId = savedReview.getId();

        // ==============================
        // AUTHENTICATION
        // ==============================

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        savedUser.getEmail(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )
                );

        // ==============================
        // CREATE BUSINESS RESPONSE
        // ==============================

        String requestBody = """
                {
                    "businessId": %d,
                    "reviewId": %d,
                    "response": "Thank you for sharing your experience. We appreciate your feedback."
                }
                """.formatted(
                        savedBusiness.getId(),
                        savedReview.getId()
                );

        mockMvc.perform(
                post("/business-responses")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().is2xxSuccessful());

        // ==============================
        // VERIFY DATABASE
        // ==============================

        Optional<BusinessResponse> savedResponse =
                businessResponseRepository
                        .findByBusinessIdAndReviewId(
                                savedBusiness.getId(),
                                savedReview.getId()
                        );

        assertTrue(
                savedResponse.isPresent(),
                "Business response should be created"
        );

        testResponseId =
                savedResponse.get().getId();

        assertNotNull(
                savedResponse.get().getId(),
                "Business response ID should be generated"
        );

        assertEquals(
                savedBusiness.getId(),
                savedResponse.get().getBusinessId()
        );

        assertEquals(
                savedReview.getId(),
                savedResponse.get().getReviewId()
        );

        assertEquals(
                "Thank you for sharing your experience. We appreciate your feedback.",
                savedResponse.get().getResponse()
        );
    }
}