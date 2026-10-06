
package com.nit.review;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class EditReviewTest {

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
    void shouldEditOwnReviewSuccessfully()
            throws Exception {

        testEmail =
                "edit-review-test-"
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
                "edit-review-test-"
                        + UUID.randomUUID()
                        + ".example.com";

        Website website =
                new Website(
                        "Edit Review Test Website",
                        "https://" + domain,
                        "Temporary website for edit testing"
                );

        website.setCanonicalDomain(domain);

        Website savedWebsite =
                websiteRepository.save(
                        website
                );

        testWebsiteId =
                savedWebsite.getId();

        Review review =
                new Review();

        review.setRating(3);

        review.setComment(
                "Original review comment."
        );

        review.setUserId(
                savedUser.getId()
        );

        review.setWebsiteId(
                testWebsiteId
        );

        review.setStatus("PENDING");

        review.setVerificationStatus(
                "UNVERIFIED"
        );

        Review savedReview =
                reviewRepository.save(
                        review
                );

        testReviewId =
                savedReview.getId();

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
                    "comment": "Updated review comment.",
                    "websiteId": %d
                }
                """.formatted(
                        testWebsiteId
                );

        mockMvc.perform(
                patch(
                        "/reviews/" + testReviewId
                )
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
                jsonPath("$.id")
                        .value(
                                testReviewId.intValue()
                        )
        )
        .andExpect(
                jsonPath("$.rating")
                        .value(5)
        )
        .andExpect(
                jsonPath("$.comment")
                        .value(
                                "Updated review comment."
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
        );
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

