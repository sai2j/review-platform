package com.nit.business;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class BusinessEmailVerificationService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(BusinessEmailVerificationService.class);

	private final JavaMailSender mailSender;

	// Mail is sent on a separate thread so a stuck mail server can never hang the request
	private final java.util.concurrent.ExecutorService mailExecutor = java.util.concurrent.Executors
			.newCachedThreadPool(runnable -> {
				Thread thread = new Thread(runnable, "business-mail-sender");
				thread.setDaemon(true);
				return thread;
			});

	@Value("${app.base-url:http://localhost:8080/review-platform}")
	private String baseUrl;

	// Sender address (the Gmail account used to send the verification mail)
	@Value("${spring.mail.username:}")
	private String mailFrom;

	// The SMTP host / port that are REALLY being used (shown in the log to spot a wrong MAIL_HOST / MAIL_PORT)
	@Value("${spring.mail.host:}")
	private String mailHost;

	@Value("${spring.mail.port:}")
	private String mailPort;

	// LOCAL TESTING / CLOUD FALLBACK: if the mail cannot be sent, write the verification link into the server console
	@Value("${app.mail.log-link-when-send-fails:false}")
	private boolean logLinkWhenSendFails;

	// Maximum seconds to wait for the mail server
	@Value("${app.mail.send-timeout-seconds:12}")
	private long sendTimeoutSeconds;

	// false = any email is allowed (good for local testing)
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
			log.error("Verification email NOT sent: spring.mail.username is empty.");
			logLinkIfEnabled(verificationLink);
			return;
		}

		log.info("Sending verification email to {} via SMTP server {}:{} (login: {})", business.getBusinessEmail(),
				mailHost, mailPort, mailFrom);

		try {
			sendWithTimeout(message);
			log.info("Verification email successfully sent to {}", business.getBusinessEmail());
		} catch (Exception ex) {
			// Render par SMTP port block/timeout hone par exception catch ho jayegi aur app crash nahi hoga
			logLinkIfEnabled(verificationLink);
			log.warn("SMTP mail sending failed on cloud server (Render firewall/port block). Verification link generated in logs. Cause: {}", rootMessage(ex));
			
			// Yahan se exception throw nahi kar rahe taaki UI/Browser par error screen na aaye
		}
	}

	private void logLinkIfEnabled(String verificationLink) {
		if (logLinkWhenSendFails) {
			log.warn("CLOUD/LOCAL FALLBACK: the mail could not be sent. Open this link in the browser to verify the "
					+ "business email manually: {}", verificationLink);
		}
	}

	private void sendWithTimeout(SimpleMailMessage message) {
		java.util.concurrent.Future<?> future = mailExecutor.submit(() -> mailSender.send(message));

		try {
			future.get(sendTimeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
		} catch (java.util.concurrent.TimeoutException ex) {
			future.cancel(true);
			throw new MailSendException("Mail server connection failed: no response within " + sendTimeoutSeconds
					+ " seconds (check internet / DNS / firewall for smtp.gmail.com:587)");
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			future.cancel(true);
			throw new MailSendException("Sending the mail was interrupted", ex);
		} catch (java.util.concurrent.ExecutionException ex) {
			Throwable cause = ex.getCause() != null ? ex.getCause() : ex;

			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}

			throw new MailSendException("Mail server connection failed", cause);
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