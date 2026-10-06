
package com.nit.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserRegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String testEmail;

    @AfterEach
    void cleanup() {

        if (testEmail != null) {

            User user =
                    userRepository.findByEmail(
                            testEmail
                    );

            if (user != null) {
                userRepository.delete(user);
            }
        }
    }

    @Test
    void shouldRegisterUserSuccessfully()
            throws Exception {

        testEmail =
                "registration-test-"
                        + UUID.randomUUID()
                        + "@example.com";

        String password =
                "StrongPassword123!";

        String requestBody =
                """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(
                        testEmail,
                        password
                );

        mockMvc.perform(
                post("/users/register")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(requestBody)
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath("$.email")
                        .value(testEmail)
        )
        .andExpect(
                jsonPath("$.role")
                        .value("USER")
        );

        User savedUser =
                userRepository.findByEmail(
                        testEmail
                );

        org.junit.jupiter.api.Assertions.assertNotNull(
                savedUser
        );

        org.junit.jupiter.api.Assertions.assertTrue(
                passwordEncoder.matches(
                        password,
                        savedUser.getPassword()
                )
        );

        org.junit.jupiter.api.Assertions.assertNotEquals(
                password,
                savedUser.getPassword()
        );
    }
}

