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
class ReviewFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldApplyRatingFilter()
            throws Exception {

        mockMvc.perform(
                get("/reviews/website/302")
                        .param("rating", "5")
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                "\"content\""
                        )
                )
        );
    }

    @Test
    void shouldApplyVerificationFilter()
            throws Exception {

        mockMvc.perform(
                get("/reviews/website/302")
                        .param(
                                "verificationStatus",
                                "VERIFIED"
                        )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                "\"content\""
                        )
                )
        );
    }
}

