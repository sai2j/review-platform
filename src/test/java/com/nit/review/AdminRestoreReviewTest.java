package com.nit.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
import com.nit.user.User;
import com.nit.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AdminRestoreReviewTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebsiteRepository websiteRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminEmail;
    private Long websiteId;
    private Long reviewId;

    @Test
    void shouldAllowAdminToRestoreHiddenReview()
            throws Exception {

        adminEmail =
                "admin-restore-test-"
                        + UUID.randomUUID()
                        + "@example.com";

        User adminUser =
                new User();

        adminUser.setEmail(adminEmail);

        adminUser.setPassword(
                passwordEncoder.encode(
                        "AdminPassword123!"
                )
        );

        adminUser.setRole("ADMIN");

        adminUser.setStatus("ACTIVE");

        User savedAdmin =
                userRepository.save(adminUser);

        String domain =
                "admin-restore-test-"
                        + UUID.randomUUID()
                        + ".example.com";

        Website website =
                new Website(
                        "Admin Restore Test Website",
                        "https://" + domain,
                        "Temporary website for restore testing"
                );

        website.setCanonicalDomain(domain);

        Website savedWebsite =
                websiteRepository.save(
                        website
                );

        websiteId =
                savedWebsite.getId();

        Review review =
                new Review();

        review.setRating(4);

        review.setComment(
                "Review for admin restore testing."
        );

        review.setUserId(
                savedAdmin.getId()
        );

        review.setWebsiteId(
                websiteId
        );

        review.setStatus("HIDDEN");

        review.setVerificationStatus(
                "UNVERIFIED"
        );

        Review savedReview =
                reviewRepository.save(
                        review
                );

        reviewId =
                savedReview.getId();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        adminEmail,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"
                                )
                        )
                );

        mockMvc.perform(
                put(
                        "/reviews/"
                                + reviewId
                                + "/status"
                                + "?status=APPROVED"
                )
                .with(
                        authentication(
                                authentication
                        )
                )
        )
        .andExpect(
                status().is2xxSuccessful()
        );

        Review restoredReview =
                reviewRepository
                        .findById(reviewId)
                        .orElse(null);

        assertEquals(
                "APPROVED",
                restoredReview.getStatus()
        );
    }

    @AfterEach
    void cleanup() {

        if (reviewId != null) {
            reviewRepository.deleteById(
                    reviewId
            );
        }

        if (websiteId != null) {
            websiteRepository.deleteById(
                    websiteId
            );
        }

        if (adminEmail != null) {

            User adminUser =
                    userRepository.findByEmail(
                            adminEmail
                    );

            if (adminUser != null) {
                userRepository.delete(
                        adminUser
                );
            }
        }
    }
}