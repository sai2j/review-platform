package com.nit.security;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RateLimitFilter extends OncePerRequestFilter {

	private static final int MAX_REQUESTS = 60;
	private static final int MAX_REGISTRATION_REQUESTS = 5;
	private static final int MAX_LOGIN_REQUESTS = 10;
	private static final int MAX_REVIEW_REQUESTS = 5;
	private static final int MAX_REPORT_REQUESTS = 5;
	private static final int MAX_VOTE_REQUESTS = 10;
	private static final long WINDOW_MILLISECONDS = 60_000;
	private final Map<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return !(uri.startsWith("/review-platform/users/")
				|| uri.startsWith("/review-platform/reviews/")
				|| "/review-platform/reviews".equals(uri)
				|| uri.startsWith("/review-platform/websites/")
				|| uri.startsWith("/review-platform/businesses/")
				|| uri.startsWith("/review-platform/business-")
				|| uri.startsWith("/review-platform/admins/")
				|| uri.startsWith("/review-platform/discovery/")
				|| uri.startsWith("/review-platform/metadata/")
				|| uri.startsWith("/review-platform/review-votes/")
				|| "/review-platform/review-votes".equals(uri)
				|| uri.startsWith("/review-platform/reports"));
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain)
			throws ServletException, IOException {
		String clientIp = request.getRemoteAddr();
		String uri = request.getRequestURI();
		long currentTime = System.currentTimeMillis();
		boolean registrationRequest = "/review-platform/users/register".equals(uri);
		boolean loginRequest = "/review-platform/users/login".equals(uri);
		boolean reviewRequest = "/review-platform/reviews".equals(uri) && "POST".equalsIgnoreCase(request.getMethod());
		boolean reportRequest = "/review-platform/reports".equals(uri) && "POST".equalsIgnoreCase(request.getMethod());
		boolean voteRequest = "/review-platform/review-votes".equals(uri)
				&& "POST".equalsIgnoreCase(request.getMethod());
		String counterKey;
		int requestLimit;
		if (registrationRequest) {
			counterKey = "REGISTER:" + clientIp;
			requestLimit = MAX_REGISTRATION_REQUESTS;
		} else if (loginRequest) {
			counterKey = "LOGIN:" + clientIp;
			requestLimit = MAX_LOGIN_REQUESTS;
		} else if (reviewRequest) {
			counterKey = "REVIEW:" + clientIp;
			requestLimit = MAX_REVIEW_REQUESTS;
		} else if (reportRequest) {
			counterKey = "REPORT:" + clientIp;
			requestLimit = MAX_REPORT_REQUESTS;
		} else if (voteRequest) {
			counterKey = "VOTE:" + clientIp;
			requestLimit = MAX_VOTE_REQUESTS;
		} else {
			counterKey = "GENERAL:" + clientIp;
			requestLimit = MAX_REQUESTS;
		}
		RequestCounter counter = requestCounters.computeIfAbsent(
				counterKey,
				key -> new RequestCounter(currentTime));
		synchronized (counter) {
			if (currentTime - counter.windowStart >= WINDOW_MILLISECONDS) {
				counter.windowStart = currentTime;
				counter.count = 0;
			}
			counter.count++;
			if (counter.count > requestLimit) {
				response.setStatus(429);
				response.setContentType("application/json");
				response.setHeader("Retry-After", "60");
				String message;
				if (registrationRequest) {
					message = "Too many registration requests. Please try again later.";
				} else if (loginRequest) {
					message = "Too many login requests. Please try again later.";
				} else if (reviewRequest) {
					message = "Too many review requests. Please try again later.";
				} else if (reportRequest) {
					message = "Too many report requests. Please try again later.";
				} else if (voteRequest) {
					message = "Too many voting requests. Please try again later.";
				} else {
					message = "Too many requests. Please try again later.";
				}
				response.getWriter().write("{\"error\":\"" + message + "\"}");
				return;
			}
		}
		filterChain.doFilter(request, response);
	}
	private static class RequestCounter {
		private long windowStart;
		private int count;
		private RequestCounter(long windowStart) {
			this.windowStart = windowStart;
			this.count = 0;
		}
	}
}