
package com.nit.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UnauthorizedAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectUnauthorizedAdminAccess()
            throws Exception {

        mockMvc.perform(
                get("/admins")
        ).andExpect(
                status().isForbidden()
        );
    }

    @Test
    void shouldRejectUnauthorizedReviewCreation()
            throws Exception {

        mockMvc.perform(
                post("/reviews")
        ).andExpect(
                status().isForbidden()
        );
    }
}

