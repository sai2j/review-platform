package com.nit.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
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

            // =========================
            // SECURITY HEADERS
            // =========================
            .headers(headers -> headers
                .contentTypeOptions(contentTypeOptions -> {})
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self' data:; " +
                        "connect-src 'self'; " +
                        "object-src 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'"
                    )
                )
                .referrerPolicy(referrerPolicy -> referrerPolicy
                    .policy(
                        org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER
                    )
                )
            )

            .authorizeHttpRequests(auth -> auth

                // =========================
                // HTML PAGES
                // =========================
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

                // =========================
                // CSS + JAVASCRIPT
                // =========================
                .requestMatchers(
                    "/css/**",
                    "/js/**"
                ).permitAll()

                // =========================
                // USER LOGIN / REGISTER
                // =========================
                .requestMatchers(
                    "/users/register",
                    "/users/login"
                ).permitAll()

                // =========================
                // WEBSITE + REVIEW VIEW
                // =========================
                .requestMatchers(
                	    "/websites/**",
                	    "/reviews/website/**",
                	    "/businesses/*",
                	    "/businesses/website/*",
                	    "/business-claims/business/*/approved"
                	).permitAll()

             // =========================
             // USER LOGIN / REGISTER
             // =========================
             .requestMatchers(
                 "/users/register",
                 "/users/login"
             ).permitAll()

             // =========================
             // BUSINESS EMAIL VERIFICATION
             // =========================
             .requestMatchers(
                 "/businesses/*/verify-email"
             ).permitAll()

                // =========================
                // SEARCH / DISCOVERY
                // =========================
                .requestMatchers(
                    "/discovery/**",
                    "/metadata/**"
                ).permitAll()

                // =========================
                // VOTE COUNT
                // =========================
                .requestMatchers(
                    "/review-votes/*/count"
                ).permitAll()

                // =========================
                // ADMIN
                // =========================
                .requestMatchers("/admins/**")
                .hasRole("ADMIN")

                // =========================
                // EVERYTHING ELSE
                // =========================
                .anyRequest()
                .authenticated()
            );

        return http.build();
    }
}