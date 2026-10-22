package com.nit.business;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class BusinessEmailVerificationService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(BusinessEmailVerificationService.class);

	private final JavaMailSender mailSender;
	@Value("${app.base-url:http://localhost:8080/review-platform}")
	private String baseUrl;

	// false = any email is allowed (good for local testing)
	// true  = email must belong to the business website domain (recommended for production)
	@Value("${app.business.require-domain-match:false}")
	private boolean requireDomainMatch;

	public BusinessEmailVerificationService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void checkEmailAllowed(String email, String officialUrl) {
		if (!requireDomainMatch) {
			return;
		}

		if (!emailMatchesWebsite(email, officialUrl)) {
			String host = hostOfUrl(officialUrl);
			throw new RuntimeException("Please use an email address of your business website domain"
					+ (host != null ? " (for example name@" + host + ")." : "."));
		}
	}

	public void sendVerificationEmail(Business business) {
		if (business.getBusinessEmail() == null || business.getBusinessEmail().isBlank()) {
			throw new RuntimeException("Business email is required");
		}

		String verificationLink = baseUrl + "/businesses/" + business.getId() + "/verify-email?token="
				+ business.getEmailVerificationToken();
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(business.getBusinessEmail());
		message.setSubject("Business Email Verification - Review Platform");
		message.setText("Hello " + business.getName() + ",\n\n" + "Please verify your business email by opening "
				+ "the following link:\n\n" + verificationLink + "\n\n"
				+ "This verification link is valid for 24 hours.\n\n" + "Review Platform");
		try {
			mailSender.send(message);
		} catch (org.springframework.mail.MailException ex) {
			log.error("Verification email could not be sent to {}: {}", business.getBusinessEmail(), ex.getMessage());
			throw new RuntimeException(
					"Could not send the verification email. The mail server is not reachable right now, please try again later.");
		}
	}

	// ---------- helpers: email domain must belong to the business website ----------

	private static boolean emailMatchesWebsite(String email, String officialUrl) {
		String emailDomain = domainOfEmail(email);
		String siteHost = hostOfUrl(officialUrl);

		if (emailDomain == null || siteHost == null) {
			return false;
		}

		return emailDomain.equals(siteHost) || emailDomain.endsWith("." + siteHost);
	}

	private static String domainOfEmail(String email) {
		if (email == null) {
			return null;
		}

		String value = email.trim().toLowerCase(java.util.Locale.ROOT);
		int at = value.lastIndexOf('@');

		if (at <= 0 || at == value.length() - 1) {
			return null;
		}

		return value.substring(at + 1);
	}

	private static String hostOfUrl(String url) {
		if (url == null || url.isBlank()) {
			return null;
		}

		String value = url.trim();

		if (!value.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
			value = "https://" + value;
		}

		try {
			String host = new java.net.URI(value).getHost();

			if (host == null) {
				return null;
			}

			host = host.toLowerCase(java.util.Locale.ROOT);

			return host.startsWith("www.") ? host.substring(4) : host;
		} catch (Exception ex) {
			return null;
		}
	}
}