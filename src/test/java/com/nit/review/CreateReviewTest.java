
package com.nit.review;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
import com.nit.user.User;
import com.nit.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class CreateReviewTest {

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

    private String testEmail;

    private Long testWebsiteId;

    private Long testReviewId;

    @Test
    void shouldCreateReviewSuccessfully()
            throws Exception {

        testEmail =
                "review-test-"
                        + UUID.randomUUID()
                        + "@example.com";

        User user =
                new User();

        user.setEmail(testEmail);

        user.setPassword(
                passwordEncoder.encode(
                        "StrongPassword123!"
                )
        );

        user.setRole("USER");
        user.setStatus("ACTIVE");

        User savedUser =
                userRepository.save(user);

        String domain =
                "review-test-"
                        + UUID.randomUUID()
                        + ".example.com";

        Website website =
                new Website(
                        "Review Test Website",
                        "https://" + domain,
                        "Temporary website for review testing"
                );

        website.setCanonicalDomain(domain);

        Website savedWebsite =
                websiteRepository.save(
                        website
                );

        testWebsiteId =
                savedWebsite.getId();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        testEmail,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )
                );

        String requestBody =
                """
                {
                    "rating": 5,
                    "comment": "Excellent experience for review testing.",
                    "websiteId": %d
                }
                """.formatted(
                        testWebsiteId
                );

        mockMvc.perform(
                post("/reviews")
                        .with(
                                authentication(
                                        authentication
                                )
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(requestBody)
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath("$.rating")
                        .value(5)
        )
        .andExpect(
                jsonPath("$.comment")
                        .value(
                                "Excellent experience for review testing."
                        )
        )
        .andExpect(
                jsonPath("$.userId")
                        .value(
                                savedUser.getId().intValue()
                        )
        )
        .andExpect(
                jsonPath("$.websiteId")
                        .value(
                                testWebsiteId.intValue()
                        )
        )
        .andExpect(
                jsonPath("$.status")
                        .value("PENDING")
        )
        .andExpect(
                jsonPath("$.verificationStatus")
                        .value("UNVERIFIED")
        );

        Review savedReview =
                reviewRepository
                        .findByWebsiteId(
                                testWebsiteId
                        )
                        .stream()
                        .filter(review ->
                                savedUser.getId()
                                        .equals(
                                                review.getUserId()
                                        )
                        )
                        .findFirst()
                        .orElse(null);

        if (savedReview != null) {

            testReviewId =
                    savedReview.getId();
        }
    }

    @AfterEach
    void cleanup() {

        if (testReviewId != null) {

            reviewRepository.deleteById(
                    testReviewId
            );
        }

        if (testWebsiteId != null) {

            websiteRepository.deleteById(
                    testWebsiteId
            );
        }

        if (testEmail != null) {

            User user =
                    userRepository.findByEmail(
                            testEmail
                    );

            if (user != null) {

                userRepository.delete(
                        user
                );
            }
        }
    }
}

