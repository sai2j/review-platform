package com.nit.business;

import org.springframework.beans.factory.annotation.Value;
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

	// SMTP sender (fallback, works on local PC)
	@Value("${spring.mail.username:}")
	private String mailFrom;

	@Value("${spring.mail.host:}")
	private String mailHost;

	@Value("${spring.mail.port:}")
	private String mailPort;

	// If the mail cannot be sent, write the verification link into the server log
	@Value("${app.mail.log-link-when-send-fails:false}")
	private boolean logLinkWhenSendFails;

	// Maximum seconds to wait for the mail server / API
	@Value("${app.mail.send-timeout-seconds:12}")
	private long sendTimeoutSeconds;

	// Brevo (HTTPS email API). Render blocks SMTP ports (25/465/587) but allows HTTPS (443).
	@Value("${app.mail.brevo.api-key:}")
	private String brevoApiKey;

	// Must be a VERIFIED sender in your Brevo account
	@Value("${app.mail.brevo.sender-email:${spring.mail.username:}}")
	private String brevoSenderEmail;

	@Value("${app.mail.brevo.sender-name:Review Platform}")
	private String brevoSenderName;

	private final java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
			.connectTimeout(java.time.Duration.ofSeconds(8)).build();

	// false = any email is allowed
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

		// 1) Preferred: HTTPS API (works on Render / any cloud)
		if (brevoApiKey != null && !brevoApiKey.isBlank()) {
			try {
				sendViaBrevo(business.getBusinessEmail(), message.getSubject(), message.getText());
				log.info("Verification email successfully sent to {} via Brevo API", business.getBusinessEmail());
				return;
			} catch (Exception ex) {
				logLinkIfEnabled(verificationLink);
				log.error("Brevo email sending failed: {}", rootMessage(ex));
				throw new RuntimeException("Verification email could not be sent: " + rootMessage(ex));
			}
		}

		// 2) Fallback: SMTP (works on local PC, usually blocked on cloud)
		if (mailFrom == null || mailFrom.isBlank()) {
			logLinkIfEnabled(verificationLink);
			throw new RuntimeException(
					"Email service is not configured (set BREVO_API_KEY or MAIL_USERNAME/MAIL_PASSWORD).");
		}

		log.info("Sending verification email to {} via SMTP server {}:{} (login: {})", business.getBusinessEmail(),
				mailHost, mailPort, mailFrom);

		try {
			sendWithTimeout(message);
			log.info("Verification email successfully sent to {}", business.getBusinessEmail());
		} catch (Exception ex) {
			logLinkIfEnabled(verificationLink);
			log.error("SMTP mail sending failed. Cause: {}", rootMessage(ex));
			// IMPORTANT: throw, so the UI shows the real error instead of a false "sent successfully"
			throw new RuntimeException(isConnectionProblem(ex)
					? "Mail server is not reachable from this server (SMTP port blocked). Configure BREVO_API_KEY."
					: "Verification email could not be sent: " + rootMessage(ex));
		}
	}

	private void sendViaBrevo(String to, String subject, String text) throws Exception {
		String json = "{\"sender\":{\"name\":\"" + jsonEscape(brevoSenderName) + "\",\"email\":\""
				+ jsonEscape(brevoSenderEmail.trim()) + "\"},\"to\":[{\"email\":\"" + jsonEscape(to.trim())
				+ "\"}],\"subject\":\"" + jsonEscape(subject) + "\",\"textContent\":\"" + jsonEscape(text) + "\"}";

		java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
				.uri(java.net.URI.create("https://api.brevo.com/v3/smtp/email"))
				.timeout(java.time.Duration.ofSeconds(sendTimeoutSeconds)).header("api-key", brevoApiKey.trim())
				.header("Content-Type", "application/json").header("Accept", "application/json")
				.POST(java.net.http.HttpRequest.BodyPublishers.ofString(json)).build();

		java.net.http.HttpResponse<String> response = httpClient.send(request,
				java.net.http.HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new RuntimeException("Brevo API error " + response.statusCode() + ": " + response.body());
		}
	}

	private static String jsonEscape(String value) {
		if (value == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		for (char c : value.toCharArray()) {
			switch (c) {
			case '"' -> sb.append("\\\"");
			case '\\' -> sb.append("\\\\");
			case '\n' -> sb.append("\\n");
			case '\r' -> sb.append("\\r");
			case '\t' -> sb.append("\\t");
			default -> {
				if (c < 0x20) {
					sb.append(String.format("\\u%04x", (int) c));
				} else {
					sb.append(c);
				}
			}
			}
		}

		return sb.toString();
	}

	private void logLinkIfEnabled(String verificationLink) {
		if (logLinkWhenSendFails) {
			log.warn("FALLBACK: the mail could not be sent. Open this link in the browser to verify the "
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