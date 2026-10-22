package com.nit.business;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
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

	// Sender address (the Gmail account used to send the verification mail)
	@Value("${spring.mail.username:}")
	private String mailFrom;

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
		if (mailFrom != null && !mailFrom.isBlank()) {
			message.setFrom(mailFrom.trim());
		}
		message.setTo(business.getBusinessEmail());
		message.setSubject("Business Email Verification - Review Platform");
		message.setText("Hello " + business.getName() + ",\n\n" + "Please verify your business email by opening "
				+ "the following link:\n\n" + verificationLink + "\n\n"
				+ "This verification link is valid for 24 hours.\n\n" + "Review Platform");
		if (mailFrom == null || mailFrom.isBlank()) {
			log.error("Verification email NOT sent: spring.mail.username is empty. "
					+ "Set spring.mail.username / spring.mail.password (or MAIL_USERNAME / MAIL_PASSWORD).");
			throw new RuntimeException(
					"Could not send the verification email. Mail is not configured on the server yet.");
		}

		try {
			mailSender.send(message);
		} catch (MailAuthenticationException ex) {
			// Wrong / revoked Gmail App Password or wrong username
			log.error("Verification email FAILED for {}: the SMTP server rejected the mail account login. "
					+ "Check spring.mail.username and spring.mail.password (use a valid Gmail App Password). "
					+ "Cause: {}", business.getBusinessEmail(), rootMessage(ex), ex);
			throw new RuntimeException("Could not send the verification email. The mail account login was rejected, "
					+ "so the mail credentials on the server need to be updated.");
		} catch (MailException ex) {
			if (isConnectionProblem(ex)) {
				// No internet, firewall / antivirus blocking port 587, wrong host or port
				log.error("Verification email FAILED for {}: could not connect to the SMTP server "
						+ "(check internet, firewall/antivirus, port 587 and spring.mail.host). "
						+ "Cause: {}", business.getBusinessEmail(), rootMessage(ex), ex);
				throw new RuntimeException(
						"Could not send the verification email. The mail server is not reachable right now, please try again later.");
			}

			// Connected, but the SMTP server refused the message (invalid recipient, sender not allowed, etc.)
			log.error("Verification email FAILED for {}: the SMTP server refused the message. Cause: {}",
					business.getBusinessEmail(), rootMessage(ex), ex);
			throw new RuntimeException("Could not send the verification email. The mail server rejected the message, "
					+ "please check the email address and try again.");
		}
	}

	// ---------- helpers: find out WHY sending the mail failed ----------

	private static Throwable rootCauseOf(Throwable error) {
		Throwable current = error;
		int guard = 0;

		while (current.getCause() != null && current.getCause() != current && guard < 20) {
			current = current.getCause();
			guard++;
		}

		return current;
	}

	private static String rootMessage(Throwable error) {
		Throwable root = rootCauseOf(error);
		String message = root.getMessage();

		return root.getClass().getSimpleName() + (message != null ? ": " + message : "");
	}

	private static boolean isConnectionProblem(Throwable error) {
		Throwable current = error;
		int guard = 0;

		while (current != null && guard < 20) {
			if (current instanceof java.net.UnknownHostException || current instanceof java.net.ConnectException
					|| current instanceof java.net.SocketException
					|| current instanceof java.net.SocketTimeoutException) {
				return true;
			}

			String message = current.getMessage() == null ? ""
					: current.getMessage().toLowerCase(java.util.Locale.ROOT);

			if (message.contains("could not connect") || message.contains("couldn't connect")
					|| message.contains("mail server connection failed") || message.contains("unknown smtp host")
					|| message.contains("connection timed out") || message.contains("connection refused")) {
				return true;
			}

			current = current.getCause() == current ? null : current.getCause();
			guard++;
		}

		return false;
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