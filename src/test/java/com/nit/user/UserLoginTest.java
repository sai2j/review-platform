
package com.nit.user;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
class UserLoginTest {

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
    void shouldLoginUserSuccessfully()
            throws Exception {

        testEmail =
                "login-test-"
                        + UUID.randomUUID()
                        + "@example.com";

        String password =
                "StrongPassword123!";

        User user =
                new User();

        user.setEmail(testEmail);

        user.setPassword(
                passwordEncoder.encode(password)
        );

        user.setRole("USER");
        user.setStatus("ACTIVE");

        User savedUser =
                userRepository.save(user);

        assertNotNull(savedUser);

        String loginRequest =
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
                post("/users/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(loginRequest)
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath("$.user.email")
                        .value(testEmail)
        )
        .andExpect(
                jsonPath("$.user.role")
                        .value("USER")
        )
        .andExpect(
                jsonPath("$.admin")
                        .value(false)
        );
    }
}

