
package com.nit.Website;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebsiteProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebsiteRepository websiteRepository;

    private Long testWebsiteId;

    @Test
    void shouldLoadWebsiteProfileById()
            throws Exception {

        String domain =
                "profile-test-"
                        + UUID.randomUUID()
                        + ".example.com";

        Website website =
                new Website(
                        "Profile Test Website",
                        "https://" + domain,
                        "Temporary website for profile testing"
                );

        website.setCanonicalDomain(domain);

        Website savedWebsite =
                websiteRepository.save(website);

        testWebsiteId =
                savedWebsite.getId();

        assertNotNull(testWebsiteId);

        mockMvc.perform(
                get("/websites/" + testWebsiteId)
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath("$.id")
                        .value(testWebsiteId.intValue())
        )
        .andExpect(
                jsonPath("$.name")
                        .value("Profile Test Website")
        )
        .andExpect(
                jsonPath("$.url")
                        .value("https://" + domain)
        )
        .andExpect(
                jsonPath("$.description")
                        .value(
                                "Temporary website for profile testing"
                        )
        )
        .andExpect(
                jsonPath("$.canonicalDomain")
                        .value(domain)
        )
        .andExpect(
                jsonPath("$.averageRating")
                        .exists()
        )
        .andExpect(
                jsonPath("$.reviewCount")
                        .exists()
        )
        .andExpect(
                jsonPath("$.claimed")
                        .value(false)
        )
        .andExpect(
                jsonPath("$.verified")
                        .value(false)
        );
    }

    @AfterEach
    void cleanup() {

        if (testWebsiteId != null) {

            websiteRepository.deleteById(
                    testWebsiteId
            );
        }
    }
}

