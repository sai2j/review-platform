package com.nit.report;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.nit.Report.ReportRepository;
import com.nit.Website.Website;
import com.nit.Website.WebsiteRepository;
import com.nit.review.Review;
import com.nit.review.ReviewRepository;
import com.nit.user.User;
import com.nit.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ReportReviewTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebsiteRepository websiteRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String testEmail;

    private Long testWebsiteId;

    private Long testReviewId;

    private Long testReportId;

    @Test
    void shouldReportReviewSuccessfully()
            throws Exception {

        testEmail =
                "report-review-test-"
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
                "report-review-test-"
                        + UUID.randomUUID()
                        + ".example.com";

        Website website =
                new Website(
                        "Report Review Test Website",
                        "https://" + domain,
                        "Temporary website for report testing"
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

        review.setRating(2);

        review.setComment(
                "Review created for report testing."
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
                    "userId": %d,
                    "reviewId": %d,
                    "reason": "Spam"
                }
                """.formatted(
                        savedUser.getId(),
                        testReviewId
                );

        mockMvc.perform(
                post("/reports")
                        .with(
                                authentication(
                                        authentication
                                )
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                requestBody
                        )
        )
        .andExpect(
                status().is2xxSuccessful()
        );

        var savedReport =
                reportRepository
                        .findAll()
                        .stream()
                        .filter(report ->
                                testReviewId.equals(
                                        report.getReviewId()
                                )
                                && savedUser.getId().equals(
                                        report.getUserId()
                                )
                                && "Spam".equalsIgnoreCase(
                                        report.getReason()
                                )
                        )
                        .findFirst()
                        .orElse(null);

        assertTrue(
                savedReport != null,
                "Review report should be saved"
        );

        testReportId =
                savedReport.getId();
    }

    @AfterEach
    void cleanup() {

        if (testReportId != null) {

            reportRepository.deleteById(
                    testReportId
            );
        }

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