package com.nit.business;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nit.Website.Website;
import com.nit.Website.WebsiteRepository;
import com.nit.Website.WebsiteService;
import com.nit.review.Review;
import com.nit.review.ReviewRepository;
import com.nit.user.User;
import com.nit.user.UserRepository;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final WebsiteRepository websiteRepository;
    private final WebsiteService websiteService;
    private final ReviewRepository reviewRepository;
    private final BusinessclaimRepository businessClaimRepository;
    private final UserRepository userRepository;

    public BusinessService(
            BusinessRepository businessRepository,
            WebsiteRepository websiteRepository,
            WebsiteService websiteService,
            ReviewRepository reviewRepository,
            BusinessclaimRepository businessClaimRepository,
            UserRepository userRepository) {

        this.businessRepository = businessRepository;
        this.websiteRepository = websiteRepository;
        this.websiteService = websiteService;
        this.reviewRepository = reviewRepository;
        this.businessClaimRepository = businessClaimRepository;
        this.userRepository = userRepository;
    }

    public Business saveBusiness(Business business) {
        return businessRepository.save(business);
    }

    public List<Business> getAllBusinesses() {
        return businessRepository.findAll();
    }

    public Business getBusinessById(Long id) {
        return businessRepository.findById(id).orElse(null);
    }

    // =========================================================
    // BUSINESS API AUTHORIZATION
    // =========================================================

    public boolean canAccessBusiness(Long businessId) {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return false;
        }

        User loggedInUser =
                userRepository.findByEmail(
                        authentication.getName());

        if (loggedInUser == null) {
            return false;
        }

        BusinessClaim approvedClaim =
                businessClaimRepository
                        .findByBusinessIdAndStatus(
                                businessId,
                                "APPROVED")
                        .orElse(null);

        if (approvedClaim == null) {
            return false;
        }

        return loggedInUser.getId()
                .equals(approvedClaim.getUserId());
    }

    // Business owner can edit only their approved claimed business
    public Business updateBusiness(Long id, Business business) {

        Business existingBusiness =
                businessRepository.findById(id).orElse(null);

        if (existingBusiness == null) {
            return null;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "You must be logged in to edit business profile");
        }

        String loggedInEmail = authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {

            throw new AccessDeniedException(
                    "User not found");
        }

        BusinessClaim approvedClaim =
                businessClaimRepository
                        .findByBusinessIdAndStatus(
                                id,
                                "APPROVED")
                        .orElse(null);

        if (approvedClaim == null
                || !loggedInUser.getId()
                        .equals(approvedClaim.getUserId())) {

            throw new AccessDeniedException(
                    "You can edit only your approved claimed business");
        }

        // Only allowed profile information can be changed
        if (business.getName() != null) {
            existingBusiness.setName(business.getName());
        }

        if (business.getDescription() != null) {
            existingBusiness.setDescription(
                    business.getDescription());
        }

        if (business.getOfficialUrl() != null) {
            existingBusiness.setOfficialUrl(
                    business.getOfficialUrl());
        }

        // Status, verification and email fields are NOT changed here

        return businessRepository.save(existingBusiness);
    }

    public Business verifyBusiness(Long id) {

        Business business =
                businessRepository.findById(id).orElse(null);

        if (business == null) {
            return null;
        }

        business.setStatus("VERIFIED");

        return businessRepository.save(business);
    }

    // =========================================================
    // BUSINESS EMAIL VERIFICATION
    // =========================================================

    public Business createEmailVerificationToken(
            Long businessId,
            String email) {

        Business business =
                businessRepository.findById(businessId).orElse(null);

        if (business == null) {
            throw new RuntimeException("Business not found");
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException(
                    "Business email is required");
        }

        if (business.getOfficialUrl() == null
                || business.getOfficialUrl().isBlank()) {

            throw new RuntimeException(
                    "Official website URL is required");
        }

        String emailDomain = getEmailDomain(email);

        String websiteDomain =
                normalizeDomain(business.getOfficialUrl());

        if (!emailDomain.equals(websiteDomain)) {

            throw new RuntimeException(
                    "Business email domain must match official website domain");
        }

        String token = UUID.randomUUID().toString();

        business.setBusinessEmail(email);
        business.setEmailVerified(false);
        business.setEmailVerificationToken(token);
        business.setEmailVerificationExpiry(
                LocalDateTime.now().plusHours(24));

        return businessRepository.save(business);
    }

    public Business verifyBusinessEmail(
            Long businessId,
            String token) {

        Business business =
                businessRepository.findById(businessId).orElse(null);

        if (business == null) {
            throw new RuntimeException("Business not found");
        }

        if (business.getEmailVerificationToken() == null
                || !business.getEmailVerificationToken()
                        .equals(token)) {

            throw new RuntimeException(
                    "Invalid verification token");
        }

        if (business.getEmailVerificationExpiry() == null
                || LocalDateTime.now().isAfter(
                        business.getEmailVerificationExpiry())) {

            throw new RuntimeException(
                    "Verification token has expired");
        }

        business.setEmailVerified(true);
        business.setEmailVerificationToken(null);
        business.setEmailVerificationExpiry(null);
        business.setStatus("VERIFIED");

        return businessRepository.save(business);
    }

    // =========================================================
    // META TAG VERIFICATION
    // =========================================================

    public Business createMetaVerificationToken(
            Long businessId) {

        Business business =
                businessRepository.findById(businessId).orElse(null);

        if (business == null) {
            throw new RuntimeException("Business not found");
        }

        if (business.getOfficialUrl() == null
                || business.getOfficialUrl().isBlank()) {

            throw new RuntimeException(
                    "Official website URL is required");
        }

        String token = UUID.randomUUID().toString();

        business.setMetaVerificationToken(token);
        business.setMetaVerified(false);

        return businessRepository.save(business);
    }

    public Business verifyBusinessMetaTag(Long businessId) {

        Business business =
                businessRepository.findById(businessId).orElse(null);

        if (business == null) {
            throw new RuntimeException("Business not found");
        }

        String token = business.getMetaVerificationToken();

        if (token == null || token.isBlank()) {
            throw new RuntimeException(
                    "Meta verification token has not been generated");
        }

        String officialUrl = business.getOfficialUrl();

        if (officialUrl == null || officialUrl.isBlank()) {
            throw new RuntimeException(
                    "Official website URL is required");
        }

        try {

            if (!officialUrl.startsWith("http://")
                    && !officialUrl.startsWith("https://")) {

                officialUrl = "https://" + officialUrl;
            }

            HttpClient client =
                    HttpClient.newBuilder()
                            .followRedirects(
                                    HttpClient.Redirect.NORMAL)
                            .build();

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(officialUrl))
                            .header(
                                    "User-Agent",
                                    "ReviewPlatformBot/1.0")
                            .GET()
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Unable to access official website");
            }

            String html = response.body();

            String expectedMetaTag =
                    "<meta name=\"review-platform-verification\" content=\""
                            + token + "\">";

            String expectedMetaTagReverse =
                    "<meta content=\"" + token
                            + "\" name=\"review-platform-verification\">";

            if (!html.contains(expectedMetaTag)
                    && !html.contains(expectedMetaTagReverse)) {

                throw new RuntimeException(
                        "Verification meta tag not found on official website");
            }

            business.setMetaVerified(true);
            business.setMetaVerificationToken(null);
            business.setStatus("VERIFIED");

            return businessRepository.save(business);

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to verify website meta tag");
        }
    }

    // =========================================================
    // DNS VERIFICATION
    // =========================================================

    public Business createDnsVerificationToken(
            Long businessId) {

        Business business =
                businessRepository.findById(businessId).orElse(null);

        if (business == null) {
            throw new RuntimeException("Business not found");
        }

        if (business.getOfficialUrl() == null
                || business.getOfficialUrl().isBlank()) {

            throw new RuntimeException(
                    "Official website URL is required");
        }

        String token = UUID.randomUUID().toString();

        business.setDnsVerificationToken(token);
        business.setDnsVerified(false);

        return businessRepository.save(business);
    }

    public Business verifyBusinessDns(Long businessId) {

        Business business =
                businessRepository.findById(businessId).orElse(null);

        if (business == null) {
            throw new RuntimeException("Business not found");
        }

        String token =
                business.getDnsVerificationToken();

        if (token == null || token.isBlank()) {

            throw new RuntimeException(
                    "DNS verification token has not been generated");
        }

        String domain =
                normalizeDomain(business.getOfficialUrl());

        if (!isDnsTokenPresent(domain, token)) {

            throw new RuntimeException(
                    "DNS verification TXT record not found");
        }

        business.setDnsVerified(true);
        business.setDnsVerificationToken(null);
        business.setStatus("VERIFIED");

        return businessRepository.save(business);
    }

    private boolean isDnsTokenPresent(
            String domain,
            String expectedToken) {

        DirContext context = null;

        try {

            Hashtable<String, String> environment =
                    new Hashtable<>();

            environment.put(
                    Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.jndi.dns.DnsContextFactory");

            context =
                    new InitialDirContext(environment);

            Attributes attributes =
                    context.getAttributes(
                            domain,
                            new String[]{"TXT"});

            Attribute txtAttribute =
                    attributes.get("TXT");

            if (txtAttribute == null) {
                return false;
            }

            for (int i = 0;
                    i < txtAttribute.size();
                    i++) {

                Object value =
                        txtAttribute.get(i);

                if (value != null) {

                    String txtValue =
                            value.toString()
                                    .replace("\"", "")
                                    .trim();

                    if (txtValue.equals(expectedToken)) {
                        return true;
                    }
                }
            }

            return false;

        } catch (NamingException e) {

            throw new RuntimeException(
                    "Unable to check DNS TXT record");

        } finally {

            if (context != null) {

                try {
                    context.close();
                } catch (NamingException ignored) {
                }
            }
        }
    }

    // =========================================================
    // OTHER BUSINESS METHODS
    // =========================================================

    public void deleteBusiness(Long id) {
        businessRepository.deleteById(id);
    }

    public Website getWebsiteForBusiness(Long businessId) {

        Business business =
                getBusinessById(businessId);

        if (business == null) {
            return null;
        }

        String officialUrl =
                business.getOfficialUrl();

        if (officialUrl == null
                || officialUrl.isBlank()) {

            return null;
        }

        String canonicalDomain =
                normalizeDomain(officialUrl);

        return websiteRepository
                .findByCanonicalDomain(canonicalDomain)
                .orElse(null);
    }

    public List<Review> getReviewsForBusiness(
            Long businessId) {

        Website website =
                getWebsiteForBusiness(businessId);

        if (website == null) {
            return List.of();
        }

        return reviewRepository
                .findByWebsiteId(website.getId());
    }

    public Business getBusinessForWebsite(
            Long websiteId) {

        Website website =
                websiteRepository
                        .findById(websiteId)
                        .orElse(null);

        if (website == null) {
            return null;
        }

        String canonicalDomain =
                website.getCanonicalDomain();

        if (canonicalDomain == null
                || canonicalDomain.isBlank()) {

            return null;
        }

        List<Business> businesses =
                businessRepository.findAll();

        for (Business business : businesses) {

            String officialUrl =
                    business.getOfficialUrl();

            if (officialUrl == null
                    || officialUrl.isBlank()) {

                continue;
            }

            String businessDomain =
                    normalizeDomain(officialUrl);

            if (canonicalDomain.equals(businessDomain)) {
                return business;
            }
        }

        return null;
    }

    private String getEmailDomain(String email) {

        String cleanEmail =
                email.trim().toLowerCase();

        int atIndex =
                cleanEmail.lastIndexOf("@");

        if (atIndex <= 0
                || atIndex == cleanEmail.length() - 1) {

            throw new RuntimeException(
                    "Invalid business email");
        }

        String domain =
                cleanEmail.substring(atIndex + 1);

        if (domain.startsWith("www.")) {
            domain = domain.substring(4);
        }

        return domain;
    }

    private String normalizeDomain(String url) {

        try {

            String cleanUrl =
                    url.trim();

            if (!cleanUrl.startsWith("http://")
                    && !cleanUrl.startsWith("https://")) {

                cleanUrl = "https://" + cleanUrl;
            }

            URI uri =
                    new URI(cleanUrl);

            String domain =
                    uri.getHost();

            if (domain == null) {

                throw new RuntimeException(
                        "Invalid website URL");
            }

            domain =
                    domain.toLowerCase();

            if (domain.startsWith("www.")) {

                domain =
                        domain.substring(4);
            }

            return domain;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid website URL");
        }
    }
}