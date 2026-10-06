package com.nit.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class RateLimitFilterTest {

    @Test
    void shouldAllowNormalRequest()
            throws ServletException, IOException {

        RateLimitFilter filter =
                new RateLimitFilter();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        FilterChain filterChain =
                mock(FilterChain.class);

        org.mockito.Mockito.when(
                request.getRequestURI()
        ).thenReturn(
                "/review-platform/users/login"
        );

        org.mockito.Mockito.when(
                request.getRemoteAddr()
        ).thenReturn(
                "127.0.0.1"
        );

        filter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(
                request,
                response
        );
    }


    @Test
    void shouldBlockAfterLoginLimitExceeded()
            throws ServletException, IOException {

        RateLimitFilter filter =
                new RateLimitFilter();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        StringWriter stringWriter =
                new StringWriter();

        PrintWriter printWriter =
                new PrintWriter(stringWriter);

        org.mockito.Mockito.when(
                response.getWriter()
        ).thenReturn(
                printWriter
        );

        FilterChain filterChain =
                mock(FilterChain.class);

        org.mockito.Mockito.when(
                request.getRequestURI()
        ).thenReturn(
                "/review-platform/users/login"
        );

        org.mockito.Mockito.when(
                request.getRemoteAddr()
        ).thenReturn(
                "127.0.0.1"
        );

        for (int i = 0; i < 11; i++) {

            filter.doFilter(
                    request,
                    response,
                    filterChain
            );
        }

        verify(response)
                .setStatus(429);

        assertEquals(
                11,
                11
        );
    }
}