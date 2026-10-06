
package com.nit.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectInvalidLoginCredentials()
            throws Exception {

        String loginRequest =
                """
                {
                    "email": "login-test-nonexistent@example.com",
                    "password": "WrongPassword123!"
                }
                """;

        mockMvc.perform(
                post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest)
        ).andExpect(
                status().is4xxClientError()
        );
    }
}

