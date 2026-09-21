package com.nit.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://127.0.0.1:8080"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.addFilterBefore(new RateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
				.headers(headers -> headers
						.contentTypeOptions(contentTypeOptions -> {
						})
						.frameOptions(frameOptions -> frameOptions.deny())
						.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; "
								+ "script-src 'self' 'unsafe-inline'; " + "style-src 'self' 'unsafe-inline'; "
								+ "img-src 'self' data: https:; " + "font-src 'self' data:; " + "connect-src 'self'; "
								+ "object-src 'none'; " + "base-uri 'self'; " + "form-action 'self'"))
						.referrerPolicy(referrerPolicy -> referrerPolicy.policy(
								org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/index.html", "/login.html", "/register.html", "/review.html",
								"/business-claim.html", "/business-dashboard.html", "/business-response.html",
								"/admin.html")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/website/**").permitAll()
						.requestMatchers("/error").permitAll()
						.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.requestMatchers("/sitemap.xml", "/website-sitemap.xml").permitAll()
						.requestMatchers("/css/**", "/js/**").permitAll()
						.requestMatchers("/users/register", "/users/login").permitAll()
						.requestMatchers("/websites/**", "/api/v1/websites/**", "/reviews/website/**", "/businesses/*",
								"/businesses/website/*", "/business-claims/business/*/approved")
						.permitAll()
						.requestMatchers("/businesses/*/verify-email").permitAll()
						.requestMatchers("/discovery/**", "/metadata/**").permitAll()
						.requestMatchers("/review-votes/*/count").permitAll()
						.requestMatchers("/admins/**").hasRole("ADMIN")
						.anyRequest().authenticated());
		return http.build();
	}
}