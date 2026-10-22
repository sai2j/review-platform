package com.nit.business;

import java.net.URI;
import java.util.Locale;

/**
 * Checks that a business email belongs to the business website domain.
 * Example: website https://www.shopsphere.com  ->  owner@shopsphere.com is OK,
 * owner@mail.shopsphere.com is OK, owner@gmail.com is NOT OK.
 */
public final class EmailDomainMatcher {

	private EmailDomainMatcher() {
	}

	public static boolean emailMatchesWebsite(String email, String officialUrl) {
		String emailDomain = domainOfEmail(email);
		String siteHost = hostOfUrl(officialUrl);

		if (emailDomain == null || siteHost == null) {
			return false;
		}

		return emailDomain.equals(siteHost) || emailDomain.endsWith("." + siteHost);
	}

	public static String domainOfEmail(String email) {
		if (email == null) {
			return null;
		}

		String value = email.trim().toLowerCase(Locale.ROOT);
		int at = value.lastIndexOf('@');

		if (at <= 0 || at == value.length() - 1) {
			return null;
		}

		return value.substring(at + 1);
	}

	public static String hostOfUrl(String url) {
		if (url == null || url.isBlank()) {
			return null;
		}

		String value = url.trim();

		if (!value.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
			value = "https://" + value;
		}

		try {
			String host = new URI(value).getHost();

			if (host == null) {
				return null;
			}

			host = host.toLowerCase(Locale.ROOT);

			return host.startsWith("www.") ? host.substring(4) : host;
		} catch (Exception ex) {
			return null;
		}
	}
}