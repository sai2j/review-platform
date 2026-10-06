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
class MetadataTest {

@Autowired
private MockMvc mockMvc;

@Test
void shouldContainBasicSeoMetadata()
        throws Exception {

    mockMvc.perform(
            get("/review.html")
    )
    .andExpect(
            status().isOk()
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "<title>Website Reviews</title>"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "name=\"description\""
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "Read website reviews, ratings, experiences, and frequently asked questions."
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "name=\"robots\""
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "index, follow"
                    )
            )
    );
}

@Test
void shouldContainDynamicSeoMetadataLogic()
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
                            "rel=\"canonical\""
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "og:title"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "og:description"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "og:url"
                    )
            )
    );
}


}
