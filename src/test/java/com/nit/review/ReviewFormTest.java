
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
class ReviewFormTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldLoadReviewPage()
            throws Exception {

        mockMvc.perform(
                get("/review.html")
        ).andExpect(
                status().isOk()
        );
    }

    @Test
    void shouldContainReviewFormElements()
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
                                "id=\"reviewForm\""
                        )
                )
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                "id=\"rating\""
                        )
                )
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                "id=\"comment\""
                        )
                )
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                "id=\"message\""
                        )
                )
        );
    }
}
