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

		if (!EmailDomainMatcher.emailMatchesWebsite(email, officialUrl)) {
			String host = EmailDomainMatcher.hostOfUrl(officialUrl);
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
}