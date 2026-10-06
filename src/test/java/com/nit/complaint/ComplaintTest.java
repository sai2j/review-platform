package com.nit.complaint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
class ComplaintTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private WebsiteRepository websiteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long testUserId;
    private Long testAdminId;
    private Long testWebsiteId;
    private Long testReviewId;
    private Long testComplaintId;

    @AfterEach
    void cleanup() {

        if (testComplaintId != null) {
            complaintRepository.deleteById(testComplaintId);
        }

        if (testReviewId != null) {
            reviewRepository.deleteById(testReviewId);
        }

        if (testWebsiteId != null) {
            websiteRepository.deleteById(testWebsiteId);
        }

        if (testAdminId != null) {
            userRepository.deleteById(testAdminId);
        }

        if (testUserId != null) {
            userRepository.deleteById(testUserId);
        }
    }

    @Test
    void shouldCreateComplaint() throws Exception {

        // ==============================
        // CREATE USER
        // ==============================

        User user = new User();

        user.setName("Complaint Test User");

        user.setEmail(
                "complaint-test-"
                        + UUID.randomUUID()
                        + "@example.com"
        );

        user.setPassword(
                passwordEncoder.encode("TestPassword123!")
        );

        user.setRole("USER");
        user.setStatus("ACTIVE");

        User savedUser =
                userRepository.save(user);

        testUserId = savedUser.getId();

        // ==============================
        // CREATE WEBSITE
        // ==============================

        Website website = new Website(
                "Complaint Test Website",
                "https://complaint-test-"
                        + UUID.randomUUID()
                        + ".example.com",
                "Website created for complaint test"
        );

        website.setCanonicalDomain(
                "complaint-test-"
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
                3,
                "Review created for complaint test",
                savedUser.getId(),
                savedWebsite.getId()
        );

        review.setStatus("APPROVED");
        review.setVerificationStatus("UNVERIFIED");

        Review savedReview =
                reviewRepository.save(review);

        testReviewId = savedReview.getId();

        // ==============================
        // USER AUTHENTICATION
        // ==============================

        Authentication userAuthentication =
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
        // CREATE COMPLAINT
        // ==============================

        var result = mockMvc.perform(
                post("/complaints")
                        .with(
                                authentication(
                                        userAuthentication
                                )
                        )
                        .param(
                                "reviewId",
                                String.valueOf(
                                        savedReview.getId()
                                )
                        )
                        .param(
                                "topic",
                                "Refund"
                        )
                        .param(
                                "description",
                                "Refund has not been processed."
                        )
        )
        .andExpect(status().is2xxSuccessful())
        .andReturn();

        // ==============================
        // VERIFY DATABASE
        // ==============================

        Optional<Complaint> savedComplaint =
                complaintRepository
                        .findByReviewId(
                                savedReview.getId()
                        )
                        .stream()
                        .filter(
                                complaint ->
                                        complaint.getUserId()
                                                .equals(
                                                        savedUser.getId()
                                                )
                        )
                        .findFirst();

        assertTrue(
                savedComplaint.isPresent(),
                "Complaint should be created"
        );

        testComplaintId =
                savedComplaint.get().getId();

        assertNotNull(
                savedComplaint.get().getId()
        );

        assertEquals(
                savedReview.getId(),
                savedComplaint.get().getReviewId()
        );

        assertEquals(
                savedUser.getId(),
                savedComplaint.get().getUserId()
        );

        assertEquals(
                "Refund",
                savedComplaint.get().getTopic()
        );

        assertEquals(
                "Refund has not been processed.",
                savedComplaint.get().getDescription()
        );

        assertEquals(
                "PENDING",
                savedComplaint.get().getStatus()
        );
    }

    @Test
    void shouldAllowAdminToResolveComplaint() throws Exception {

        // ==============================
        // CREATE USER
        // ==============================

        User user = new User();

        user.setName("Complaint Owner");

        user.setEmail(
                "complaint-owner-"
                        + UUID.randomUUID()
                        + "@example.com"
        );

        user.setPassword(
                passwordEncoder.encode("TestPassword123!")
        );

        user.setRole("USER");
        user.setStatus("ACTIVE");

        User savedUser =
                userRepository.save(user);

        testUserId = savedUser.getId();

        // ==============================
        // CREATE ADMIN
        // ==============================

        User admin = new User();

        admin.setName("Complaint Admin");

        admin.setEmail(
                "complaint-admin-"
                        + UUID.randomUUID()
                        + "@example.com"
        );

        admin.setPassword(
                passwordEncoder.encode("AdminPassword123!")
        );

        admin.setRole("ADMIN");
        admin.setStatus("ACTIVE");

        User savedAdmin =
                userRepository.save(admin);

        testAdminId = savedAdmin.getId();

        // ==============================
        // CREATE WEBSITE
        // ==============================

        Website website = new Website(
                "Complaint Resolution Website",
                "https://complaint-resolution-"
                        + UUID.randomUUID()
                        + ".example.com",
                "Website created for resolution test"
        );

        website.setCanonicalDomain(
                "complaint-resolution-"
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
                2,
                "Review for complaint resolution",
                savedUser.getId(),
                savedWebsite.getId()
        );

        review.setStatus("APPROVED");
        review.setVerificationStatus("UNVERIFIED");

        Review savedReview =
                reviewRepository.save(review);

        testReviewId = savedReview.getId();

        // ==============================
        // CREATE COMPLAINT DIRECTLY
        // ==============================

        Complaint complaint =
                new Complaint(
                        savedReview.getId(),
                        savedUser.getId(),
                        "Service",
                        "Service issue needs attention."
                );

        Complaint savedComplaint =
                complaintRepository.save(complaint);

        testComplaintId =
                savedComplaint.getId();

        // ==============================
        // ADMIN AUTHENTICATION
        // ==============================

        Authentication adminAuthentication =
                new UsernamePasswordAuthenticationToken(
                        savedAdmin.getEmail(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"
                                )
                        )
                );

        // ==============================
        // UPDATE COMPLAINT RESOLUTION
        // ==============================

        mockMvc.perform(
                put(
                        "/complaints/"
                                + savedComplaint.getId()
                                + "/resolution"
                )
                .with(
                        authentication(
                                adminAuthentication
                        )
                )
                .param(
                        "status",
                        "RESOLVED"
                )
                .param(
                        "resolution",
                        "Issue has been resolved successfully."
                )
        )
        .andExpect(status().is2xxSuccessful());

        // ==============================
        // VERIFY DATABASE
        // ==============================

        Complaint updatedComplaint =
                complaintRepository
                        .findById(
                                savedComplaint.getId()
                        )
                        .orElse(null);

        assertNotNull(updatedComplaint);

        assertEquals(
                "RESOLVED",
                updatedComplaint.getStatus()
        );

        assertEquals(
                "Issue has been resolved successfully.",
                updatedComplaint.getResolution()
        );
    }
}