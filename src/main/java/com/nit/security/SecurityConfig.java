package com.nit.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                /* =========================
                   HTML PAGES
                ========================= */

                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login.html",
                    "/register.html",
                    "/review.html",
                    "/business-claim.html",
                    "/business-dashboard.html",
                    "/business-response.html",
                    "/admin.html"
                ).permitAll()


                /* =========================
                   CSS + JAVASCRIPT
                ========================= */

                .requestMatchers(
                    "/css/**",
                    "/js/**"
                ).permitAll()


                /* =========================
                   USER LOGIN / REGISTER
                ========================= */

                .requestMatchers(
                    "/users/register",
                    "/users/login"
                ).permitAll()


                /* =========================
                   WEBSITE + REVIEW VIEW
                ========================= */

                .requestMatchers(
                    "/websites/**",
                    "/reviews/website/**"
                ).permitAll()


                /* =========================
                   SEARCH / DISCOVERY
                ========================= */

                .requestMatchers(
                    "/discovery/**",
                    "/metadata/**"
                ).permitAll()


                /* =========================
                   VOTE COUNT
                ========================= */

                .requestMatchers(
                    "/review-votes/*/count"
                ).permitAll()


                /* =========================
                   BUSINESS RESPONSE
                ========================= */

                .requestMatchers(
                    "/business-responses/**"
                ).permitAll()


                /* =========================
                   ADMIN
                ========================= */

                .requestMatchers("/admins/**")
                .hasRole("ADMIN")


                /* =========================
                   EVERYTHING ELSE
                ========================= */

                .anyRequest()
                .authenticated()
            );


        return http.build();
    }
}