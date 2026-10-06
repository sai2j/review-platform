
package com.nit.Website;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebsiteSearchTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebsiteRepository websiteRepository;

    private Long testWebsiteId;

    private String testWebsiteName;

    @AfterEach
    void cleanup() {

        if (testWebsiteId != null) {

            websiteRepository.deleteById(
                    testWebsiteId
            );
        }
    }

    @Test
    void shouldFindWebsiteByName()
            throws Exception {

        testWebsiteName =
                "Search Test Website "
                        + UUID.randomUUID();

        Website website =
                new Website(
                        testWebsiteName,
                        "https://search-test-"
                                + UUID.randomUUID()
                                + ".example.com",
                        "Temporary website for search testing"
                );

        website.setCanonicalDomain(
                website.getUrl()
        );

        Website savedWebsite =
                websiteRepository.save(
                        website
                );

        testWebsiteId =
                savedWebsite.getId();

        mockMvc.perform(
                get("/websites/search")
                        .param(
                                "q",
                                testWebsiteName
                        )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath(
                        "$[*].name",
                        hasItem(testWebsiteName)
                )
        );
    }
}

