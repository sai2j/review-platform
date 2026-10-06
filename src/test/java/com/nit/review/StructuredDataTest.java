package com.nit.review;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class StructuredDataTest {


@Autowired
private MockMvc mockMvc;

@Test
void shouldContainStructuredDataImplementation()
        throws Exception {

    mockMvc.perform(
            get("/js/review.js")
    )
    .andExpect(
            status().isOk()
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "application/ld+json"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "https://schema.org"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "BreadcrumbList"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "Organization"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "WebSite"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "AggregateRating"
                    )
            )
    );
}


}
