package com.nit.review;
import com.nit.dto.WebsiteRankingDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nit.Report.ReportRepository;
import com.nit.Website.WebsiteRepository;
import com.nit.admin.AdminRepository;
import com.nit.audit.AuditLogService;
import com.nit.business.BusinessResponseRepository;
import com.nit.business.Business;
import com.nit.business.BusinessRepository;
import com.nit.business.BusinessclaimRepository;
import com.nit.notification.NotificationService;
import com.nit.dto.ReviewRequestDTO;
import com.nit.dto.ReviewResponseDTO;
import com.nit.user.User;
import com.nit.user.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ReviewService {

    public final ReviewRepository reviewRepository;

    private final UserRepository userRepository;
    private final WebsiteRepository websiteRepository;
    private final AdminRepository adminRepository;
    private final ReviewVoteRepository reviewVoteRepository;
    private final ReportRepository reportRepository;
    private final BusinessResponseRepository businessResponseRepository;
    private final AuditLogService auditLogService;

    private final NotificationService notificationService;
    private final BusinessRepository businessRepository;
    private final BusinessclaimRepository businessclaimRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            WebsiteRepository websiteRepository,
            AdminRepository adminRepository,
            ReviewVoteRepository reviewVoteRepository,
            ReportRepository reportRepository,
            BusinessResponseRepository businessResponseRepository,
            AuditLogService auditLogService,
            NotificationService notificationService,
            BusinessRepository businessRepository,
            BusinessclaimRepository businessclaimRepository) {

        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.websiteRepository = websiteRepository;
        this.adminRepository = adminRepository;
        this.reviewVoteRepository = reviewVoteRepository;
        this.reportRepository = reportRepository;
        this.businessResponseRepository = businessResponseRepository;
        this.auditLogService = auditLogService;

        this.notificationService = notificationService;
        this.businessRepository = businessRepository;
        this.businessclaimRepository = businessclaimRepository;
    }

    // ==============================
    // CREATE REVIEW
    // ==============================

    public ReviewResponseDTO saveReview(ReviewRequestDTO request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String loggedInEmail =
                authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {
            throw new RuntimeException("User not found");
        }

        if (request.getWebsiteId() == null
                || !websiteRepository.existsById(
                        request.getWebsiteId())) {

            throw new RuntimeException(
                    "Website does not exist");
        }

        String comment =
                request.getComment();

        // DUPLICATE REVIEW CHECK

        if (comment != null
                && !comment.isBlank()
                && reviewRepository
                        .existsByUserIdAndWebsiteIdAndComment(
                                loggedInUser.getId(),
                                request.getWebsiteId(),
                                comment)) {

            throw new RuntimeException(
                    "Duplicate review is not allowed");
        }

        // NEAR DUPLICATE REVIEW CHECK

        if (comment != null
                && !comment.isBlank()) {

            List<Review> existingReviews =
                    reviewRepository.findByWebsiteId(
                            request.getWebsiteId());

            String normalizedComment =
                    normalizeReviewText(comment);

            for (Review existingReview :
                    existingReviews) {

                if (existingReview.getUserId() == null
                        || !existingReview.getUserId()
                                .equals(loggedInUser.getId())) {

                    continue;
                }

                if (existingReview.getComment() == null
                        || existingReview.getComment().isBlank()) {

                    continue;
                }

                String existingNormalizedComment =
                        normalizeReviewText(
                                existingReview.getComment());

                double similarity =
                        calculateSimilarity(
                                normalizedComment,
                                existingNormalizedComment);

                if (similarity >= 0.90) {

                    throw new RuntimeException(
                            "Near-duplicate review is not allowed");
                }
            }
        }

        // REVIEW VELOCITY CHECK

        LocalDateTime velocityStartTime =
                LocalDateTime.now()
                        .minusMinutes(10);

        long recentReviewCount =
                reviewRepository
                        .countByUserIdAndCreatedAtAfter(
                                loggedInUser.getId(),
                                velocityStartTime);

        if (recentReviewCount >= 5) {

            throw new RuntimeException(
                    "Too many reviews submitted in a short time. "
                    + "Please try again later.");
        }

        // COORDINATED PATTERN CHECK

        boolean coordinatedPatternDetected =
                detectCoordinatedPattern(
                        request.getWebsiteId(),
                        loggedInUser.getId(),
                        comment,
                        request.getRating());

        // CONTENT MODERATION CHECKS

        boolean spamDetected =
                isSpamReview(comment);

        boolean advertisingDetected =
                isAdvertisingReview(comment);

        boolean maliciousLinkDetected =
                containsMaliciousLink(comment);

        boolean harassmentDetected =
                isHarassmentReview(comment);

        boolean personalInformationDetected =
                containsPersonalInformation(comment);

        // CREATE REVIEW

        Review review =
                new Review();

        review.setRating(
                request.getRating());

        review.setComment(
                request.getComment());

        review.setUserId(
                loggedInUser.getId());

        review.setWebsiteId(
                request.getWebsiteId());

        review.setDeliveryRating(
                request.getDeliveryRating());

        review.setSupportRating(
                request.getSupportRating());

        review.setRefundRating(
                request.getRefundRating());

        review.setProductRating(
                request.getProductRating());

        review.setPricingRating(
                request.getPricingRating());

        review.setStatus("PENDING");

        if (coordinatedPatternDetected
                || spamDetected
                || advertisingDetected
                || maliciousLinkDetected
                || harassmentDetected
                || personalInformationDetected) {

            review.setVerificationStatus(
                    "UNDER_REVIEW");

        } else {

            review.setVerificationStatus(
                    "UNVERIFIED");
        }

        Review savedReview =
                reviewRepository.save(review);

        // ==============================
        // NOTIFICATION
        // ==============================
        // Notify approved business owner
        // when a new review is submitted.

        Business business =
                getBusinessForWebsiteWithoutService(
                        request.getWebsiteId());

        if (business != null) {

            businessclaimRepository
                    .findByBusinessIdAndStatus(
                            business.getId(),
                            "APPROVED")
                    .ifPresent(claim -> {

                        Long ownerUserId =
                                claim.getUserId();

                        if (ownerUserId != null
                                && !ownerUserId.equals(
                                        loggedInUser.getId())) {

                            notificationService
                                    .createNotification(
                                            ownerUserId,
                                            "REVIEW_SUBMITTED",
                                            "A new review has been submitted for your business.");
                        }
                    });
        }

        return convertToResponseDTO(
                savedReview);
    }

    // ==============================
    // FIND BUSINESS FOR WEBSITE
    // ==============================

    private Business getBusinessForWebsiteWithoutService(
            Long websiteId) {

        com.nit.Website.Website website =
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

        for (Business business :
                businesses) {

            String officialUrl =
                    business.getOfficialUrl();

            if (officialUrl == null
                    || officialUrl.isBlank()) {

                continue;
            }

            String businessDomain;

            try {

                String cleanUrl =
                        officialUrl.trim();

                if (!cleanUrl.startsWith("http://")
                        && !cleanUrl.startsWith("https://")) {

                    cleanUrl =
                            "https://" + cleanUrl;
                }

                java.net.URI uri =
                        new java.net.URI(cleanUrl);

                businessDomain =
                        uri.getHost();

                if (businessDomain == null) {
                    continue;
                }

                businessDomain =
                        businessDomain.toLowerCase();

                if (businessDomain.startsWith("www.")) {

                    businessDomain =
                            businessDomain.substring(4);
                }

            } catch (Exception e) {

                continue;
            }

            if (canonicalDomain.equalsIgnoreCase(
                    businessDomain)) {

                return business;
            }
        }

        return null;
    }

    // ==============================
    // SPAM REVIEW CHECK
    // ==============================

    private boolean isSpamReview(
            String comment) {

        if (comment == null
                || comment.isBlank()) {

            return false;
        }

        String text =
                comment.trim();

        String normalized =
                text.toLowerCase()
                        .replaceAll(
                                "\\s+",
                                " ")
                        .trim();

        if (normalized.matches(
                ".*([\\p{L}\\p{N}])\\1{4,}.*")) {

            return true;
        }

        String[] words =
                normalized.split("\\s+");

        if (words.length >= 5) {

            for (int i = 0;
                    i <= words.length - 5;
                    i++) {

                if (words[i].equals(words[i + 1])
                        && words[i].equals(words[i + 2])
                        && words[i].equals(words[i + 3])
                        && words[i].equals(words[i + 4])) {

                    return true;
                }
            }
        }

        if (text.length() >= 10) {

            int symbolCount = 0;

            for (int i = 0;
                    i < text.length();
                    i++) {

                char ch =
                        text.charAt(i);

                if (!Character.isLetterOrDigit(ch)
                        && !Character.isWhitespace(ch)) {

                    symbolCount++;
                }
            }

            double symbolRatio =
                    (double) symbolCount
                            / text.length();

            if (symbolRatio > 0.60) {
                return true;
            }
        }

        if (words.length >= 6) {

            for (int i = 0;
                    i < words.length - 2;
                    i++) {

                String first =
                        words[i];

                String second =
                        words[i + 1];

                if (first.length() > 1
                        && first.equals(second)) {

                    int repeatedCount = 2;

                    for (int j = i + 2;
                            j < words.length;
                            j++) {

                        if (words[j].equals(first)) {

                            repeatedCount++;

                        } else {

                            break;
                        }
                    }

                    if (repeatedCount >= 3) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // ==============================
    // WEBSITE RANKING
    // ==============================

    public List<WebsiteRankingDTO> getWebsiteRanking() {

        List<Object[]> results =
                reviewRepository.findWebsiteRanking();

        List<WebsiteRankingDTO> rankings =
                new java.util.ArrayList<>();

        int rank = 1;

        for (Object[] result : results) {

            Long websiteId =
                    ((Number) result[0]).longValue();

            Double averageRating =
                    result[1] == null
                            ? 0.0
                            : ((Number) result[1]).doubleValue();

            Long reviewCount =
                    ((Number) result[2]).longValue();

            averageRating =
                    Math.round(averageRating * 10.0) / 10.0;

            rankings.add(
                    new WebsiteRankingDTO(
                            websiteId,
                            averageRating,
                            reviewCount,
                            rank
                    )
            );

            rank++;
        }

        return rankings;
    }
    // ==============================
    // ADVERTISING CHECK
    // ==============================

    private boolean isAdvertisingReview(
            String comment) {

        if (comment == null
                || comment.isBlank()) {

            return false;
        }

        String text =
                comment.toLowerCase()
                        .replaceAll(
                                "\\s+",
                                " ")
                        .trim();

        String[] promotionalPhrases = {

                "buy now",
                "order now",
                "shop now",
                "limited offer",
                "special offer",
                "exclusive offer",
                "best deal",
                "great deal",
                "huge discount",
                "special discount",
                "discount available",
                "use my code",
                "use code",
                "promo code",
                "coupon code",
                "referral code",
                "refer and earn",
                "sign up with my code",
                "contact me",
                "dm me",
                "message me",
                "whatsapp me",
                "call me",
                "visit my store",
                "visit my shop",
                "check out my shop",
                "check my profile",
                "follow me",
                "subscribe to me",
                "earn money",
                "make money"
        };

        for (String phrase :
                promotionalPhrases) {

            if (text.contains(phrase)) {
                return true;
            }
        }

        boolean hasPromotionWord =
                text.contains("discount")
                || text.contains("coupon")
                || text.contains("promo")
                || text.contains("offer")
                || text.contains("sale")
                || text.contains("deal")
                || text.contains("referral");

        boolean hasActionWord =
                text.contains("buy")
                || text.contains("order")
                || text.contains("shop")
                || text.contains("purchase")
                || text.contains("use")
                || text.contains("contact")
                || text.contains("call")
                || text.contains("message")
                || text.contains("visit")
                || text.contains("follow");

        return hasPromotionWord
                && hasActionWord;
    }

    // ==============================
    // MALICIOUS LINK CHECK
    // ==============================

    private boolean containsMaliciousLink(
            String comment) {

        if (comment == null
                || comment.isBlank()) {

            return false;
        }

        String text =
                comment.toLowerCase()
                        .trim();

        if (text.contains("javascript:")
                || text.contains("vbscript:")
                || text.contains("data:text/html")
                || text.contains("data:application/")
                || text.contains("file://")) {

            return true;
        }

        if (text.contains("<script")
                || text.contains("</script")
                || text.contains("onerror=")
                || text.contains("onload=")
                || text.contains("onclick=")
                || text.contains("onmouseover=")) {

            return true;
        }

        if (text.matches(
                ".*https?://[^\\s/@]+:[^\\s/@]+@.*")) {

            return true;
        }

        if (text.contains(
                "%6a%61%76%61%73%63%72%69%70%74")
                || text.contains(
                "%76%62%73%63%72%69%70%74")) {

            return true;
        }

        return text.matches(
                ".*https?://(\\d{1,3}\\.){3}\\d{1,3}([/:?#].*)?.*");
    }

    // ==============================
    // HARASSMENT CHECK
    // ==============================

    private boolean isHarassmentReview(
            String comment) {

        if (comment == null
                || comment.isBlank()) {

            return false;
        }

        String text =
                comment.toLowerCase()
                        .replaceAll(
                                "\\s+",
                                " ")
                        .trim();

        String[] abusivePhrases = {

                "shut up",
                "you are stupid",
                "you are an idiot",
                "you are a moron",
                "you are useless",
                "you are pathetic",
                "you are worthless",
                "go to hell",
                "piece of garbage",
                "piece of trash",
                "fool",
                "idiot",
                "moron",
                "stupid",
                "pathetic",
                "useless",
                "worthless",
                "bastard",
                "asshole",
                "fuck you"
        };

        for (String phrase :
                abusivePhrases) {

            if (text.contains(phrase)) {
                return true;
            }
        }

        String[] threateningPhrases = {

                "i will kill you",
                "i'll kill you",
                "kill you",
                "i will hurt you",
                "i'll hurt you",
                "hurt you",
                "i will beat you",
                "i'll beat you",
                "beat you up",
                "i will find you",
                "i'll find you",
                "find you and hurt",
                "come to your house",
                "you will regret this",
                "you will pay for this",
                "i will destroy you",
                "i'll destroy you"
        };

        for (String phrase :
                threateningPhrases) {

            if (text.contains(phrase)) {
                return true;
            }
        }

        String[] personalAttackWords = {

                "idiot",
                "moron",
                "stupid",
                "pathetic",
                "useless",
                "worthless"
        };

        int attackCount = 0;

        for (String word :
                personalAttackWords) {

            if (text.contains(word)) {
                attackCount++;
            }
        }

        return attackCount >= 2;
    }

    // ==============================
    // PERSONAL INFORMATION CHECK
    // ==============================

    private boolean containsPersonalInformation(
            String comment) {

        if (comment == null
                || comment.isBlank()) {

            return false;
        }

        String text =
                comment.trim();

        String lowerText =
                text.toLowerCase();

        if (text.matches(
                ".*[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}.*")) {

            return true;
        }

        if (text.matches(
                ".*(?<!\\d)(?:\\+91[\\s-]?)?[6-9]\\d{9}(?!\\d).*")) {

            return true;
        }

        if (text.toUpperCase().matches(
                ".*(?<![A-Z0-9])[A-Z]{5}\\d{4}[A-Z](?![A-Z0-9]).*")) {

            return true;
        }

        if (text.matches(
                ".*(?<!\\d)[2-9]\\d{3}[ -]?\\d{4}[ -]?\\d{4}(?!\\d).*")) {

            return true;
        }

        if (lowerText.matches(
                ".*\\b(account number|account no|bank account|a\\/c no|a\\/c number)\\b.*\\d{8,18}.*")) {

            return true;
        }

        String digitsOnly =
                text.replaceAll(
                        "[^0-9]",
                        "");

        if (digitsOnly.length() >= 13
                && digitsOnly.length() <= 19
                && text.matches(
                        ".*(?<!\\d)(?:\\d[ -]?){13,19}(?!\\d).*")) {

            if (passesLuhnCheck(
                    digitsOnly)) {

                return true;
            }
        }

        String[] personalInformationLabels = {

                "my phone number",
                "my mobile number",
                "my email",
                "my email id",
                "my address",
                "my home address",
                "my bank details",
                "my account number",
                "my card number",
                "my aadhaar number",
                "my pan number"
        };

        for (String label :
                personalInformationLabels) {

            if (lowerText.contains(label)) {
                return true;
            }
        }

        return false;
    }

    // ==============================
    // LUHN CHECK
    // ==============================

    private boolean passesLuhnCheck(
            String number) {

        int sum = 0;
        boolean doubleDigit = false;

        for (int i = number.length() - 1;
                i >= 0;
                i--) {

            int digit =
                    number.charAt(i) - '0';

            if (doubleDigit) {

                digit = digit * 2;

                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;

            doubleDigit =
                    !doubleDigit;
        }

        return sum % 10 == 0;
    }

    // ==============================
    // COORDINATED PATTERN
    // ==============================

    private boolean detectCoordinatedPattern(
            Long websiteId,
            Long currentUserId,
            String currentComment,
            Integer currentRating) {

        if (currentComment == null
                || currentComment.isBlank()) {

            return false;
        }

        List<Review> existingReviews =
                reviewRepository.findByWebsiteId(
                        websiteId);

        LocalDateTime patternStartTime =
                LocalDateTime.now()
                        .minusMinutes(10);

        String normalizedCurrentComment =
                normalizeReviewText(
                        currentComment);

        int matchingUsers = 0;

        List<Long> matchedUserIds =
                new java.util.ArrayList<>();

        for (Review existingReview :
                existingReviews) {

            if (existingReview.getUserId()
                    == null) {

                continue;
            }

            if (existingReview.getUserId()
                    .equals(currentUserId)) {

                continue;
            }

            if (matchedUserIds.contains(
                    existingReview.getUserId())) {

                continue;
            }

            LocalDateTime createdAt =
                    existingReview.getCreatedAt();

            if (createdAt == null
                    || createdAt.isBefore(
                            patternStartTime)) {

                continue;
            }

            if (currentRating != null
                    && existingReview.getRating()
                            != null
                    && !currentRating.equals(
                            existingReview.getRating())) {

                continue;
            }

            if (existingReview.getComment()
                    == null
                    || existingReview.getComment()
                            .isBlank()) {

                continue;
            }

            String normalizedExistingComment =
                    normalizeReviewText(
                            existingReview.getComment());

            double similarity =
                    calculateSimilarity(
                            normalizedCurrentComment,
                            normalizedExistingComment);

            if (similarity >= 0.90) {

                matchedUserIds.add(
                        existingReview.getUserId());

                matchingUsers++;

                if (matchingUsers >= 2) {
                    return true;
                }
            }
        }

        return false;
    }

    // ==============================
    // NORMALIZE REVIEW TEXT
    // ==============================

    private String normalizeReviewText(
            String text) {

        if (text == null) {
            return "";
        }

        return text.toLowerCase()
                .replaceAll(
                        "[^a-z0-9\\s]",
                        "")
                .replaceAll(
                        "\\s+",
                        " ")
                .trim();
    }

    // ==============================
    // SIMILARITY
    // ==============================

    private double calculateSimilarity(
            String first,
            String second) {

        if (first.isEmpty()
                && second.isEmpty()) {

            return 1.0;
        }

        if (first.isEmpty()
                || second.isEmpty()) {

            return 0.0;
        }

        if (first.equals(second)) {
            return 1.0;
        }

        int distance =
                levenshteinDistance(
                        first,
                        second);

        int maxLength =
                Math.max(
                        first.length(),
                        second.length());

        return 1.0
                - ((double) distance
                        / maxLength);
    }

    // ==============================
    // LEVENSHTEIN DISTANCE
    // ==============================

    private int levenshteinDistance(
            String first,
            String second) {

        int[][] distance =
                new int[
                        first.length() + 1
                ][
                        second.length() + 1
                ];

        for (int i = 0;
                i <= first.length();
                i++) {

            distance[i][0] = i;
        }

        for (int j = 0;
                j <= second.length();
                j++) {

            distance[0][j] = j;
        }

        for (int i = 1;
                i <= first.length();
                i++) {

            for (int j = 1;
                    j <= second.length();
                    j++) {

                int cost =
                        first.charAt(i - 1)
                                == second.charAt(j - 1)
                                ? 0
                                : 1;

                distance[i][j] =
                        Math.min(
                                Math.min(
                                        distance[i - 1][j] + 1,
                                        distance[i][j - 1] + 1),
                                distance[i - 1][j - 1]
                                        + cost);
            }
        }

        return distance[
                first.length()
        ][
                second.length()
        ];
    }

    // ==============================
    // GET REVIEWS BY WEBSITE
    // ==============================

    public List<Review> getReviewsByWebsiteId(
            Long websiteId) {

        return reviewRepository.findByWebsiteId(
                websiteId);
    }

    public Page<ReviewResponseDTO>
    getReviewsByWebsiteId(
            Long websiteId,
            Pageable pageable) {

        return reviewRepository
                .findByWebsiteId(
                        websiteId,
                        pageable)
                .map(this::convertToResponseDTO);
    }

    public Page<ReviewResponseDTO>
    getApprovedReviewsByWebsiteId(
            Long websiteId,
            Pageable pageable) {

        return reviewRepository
                .findByWebsiteIdAndStatusIgnoreCase(
                        websiteId,
                        "APPROVED",
                        pageable)
                .map(this::convertToResponseDTO);
    }

    // ==============================
    // RATING FILTER
    // ==============================

    public Page<ReviewResponseDTO>
    getReviewsByWebsiteIdAndRating(
            Long websiteId,
            Integer rating,
            Pageable pageable) {

        return reviewRepository
                .findByWebsiteIdAndRating(
                        websiteId,
                        rating,
                        pageable)
                .map(this::convertToResponseDTO);
    }

    // ==============================
    // VERIFICATION STATUS FILTER
    // ==============================

    public Page<ReviewResponseDTO>
    getReviewsByWebsiteIdAndVerificationStatus(
            Long websiteId,
            String verificationStatus,
            Pageable pageable) {

        return reviewRepository
                .findByWebsiteIdAndVerificationStatus(
                        websiteId,
                        verificationStatus,
                        pageable)
                .map(this::convertToResponseDTO);
    }

    // ==============================
    // EXPERIENCE TYPE FILTER
    // ==============================

    public Page<ReviewResponseDTO>
    getReviewsByWebsiteIdAndExperienceType(
            Long websiteId,
            String experienceType,
            Integer experienceRating,
            Pageable pageable) {

        if (experienceType == null
                || experienceType.isBlank()) {

            throw new RuntimeException(
                    "Experience type is required");
        }

        if (experienceRating == null
                || experienceRating < 1
                || experienceRating > 5) {

            throw new RuntimeException(
                    "Experience rating must be between 1 and 5");
        }

        String type =
                experienceType
                        .trim()
                        .toLowerCase();

        Page<Review> reviews;

        switch (type) {

            case "delivery":

                reviews =
                        reviewRepository
                                .findByWebsiteIdAndDeliveryRating(
                                        websiteId,
                                        experienceRating,
                                        pageable);

                break;

            case "support":

                reviews =
                        reviewRepository
                                .findByWebsiteIdAndSupportRating(
                                        websiteId,
                                        experienceRating,
                                        pageable);

                break;

            case "refund":

                reviews =
                        reviewRepository
                                .findByWebsiteIdAndRefundRating(
                                        websiteId,
                                        experienceRating,
                                        pageable);

                break;

            case "product":

                reviews =
                        reviewRepository
                                .findByWebsiteIdAndProductRating(
                                        websiteId,
                                        experienceRating,
                                        pageable);

                break;

            case "pricing":

                reviews =
                        reviewRepository
                                .findByWebsiteIdAndPricingRating(
                                        websiteId,
                                        experienceRating,
                                        pageable);

                break;

            default:

                throw new RuntimeException(
                        "Experience type must be delivery, "
                        + "support, refund, product or pricing");
        }

        return reviews.map(
                this::convertToResponseDTO);
    }

    // ==============================
    // DATE FILTER
    // ==============================

    public Page<ReviewResponseDTO>
    getReviewsByWebsiteIdAndDate(
            Long websiteId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        if (startDate == null
                && endDate == null) {

            throw new RuntimeException(
                    "At least one date is required");
        }

        if (startDate != null
                && endDate != null
                && startDate.isAfter(endDate)) {

            throw new RuntimeException(
                    "Start date cannot be after end date");
        }

        LocalDateTime startDateTime;

        if (startDate != null) {

            startDateTime =
                    startDate.atStartOfDay();

        } else {

            startDateTime =
                    LocalDate
                            .of(1900, 1, 1)
                            .atStartOfDay();
        }

        LocalDateTime endDateTime;

        if (endDate != null) {

            endDateTime =
                    endDate.atTime(
                            23,
                            59,
                            59,
                            999999999);

        } else {

            endDateTime =
                    LocalDate
                            .of(9999, 12, 31)
                            .atTime(
                                    23,
                                    59,
                                    59,
                                    999999999);
        }

        return reviewRepository
                .findByWebsiteIdAndCreatedAtBetween(
                        websiteId,
                        startDateTime,
                        endDateTime,
                        pageable)
                .map(this::convertToResponseDTO);
    }

    // ==============================
    // GET ALL REVIEWS
    // ==============================

    public List<Review> getAllReviews() {

        return reviewRepository.findAll();
    }

    public Page<ReviewResponseDTO>
    getAllReviews(Pageable pageable) {

        return reviewRepository
                .findAll(pageable)
                .map(this::convertToResponseDTO);
    }

    // ==============================
    // GET PENDING REVIEWS
    // ==============================

    public List<Review> getPendingReviews() {

        return reviewRepository.findByStatus(
                "PENDING");
    }

    // ==============================
    // GET HIDDEN REVIEWS
    // ==============================

    public List<Review> getHiddenReviews() {

        return reviewRepository.findByStatus(
                "HIDDEN");
    }

    // ==============================
    // GET REVIEW BY ID
    // ==============================

    public Review getReviewById(Long id) {

        return reviewRepository
                .findById(id)
                .orElse(null);
    }

    // ==============================
    // UPDATE REVIEW
    // ==============================

    public ReviewResponseDTO updateReview(
            Long id,
            ReviewRequestDTO request) {

        Review existingReview =
                reviewRepository
                        .findById(id)
                        .orElse(null);

        if (existingReview == null) {
            return null;
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String loggedInEmail =
                authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(
                        loggedInEmail);

        if (loggedInUser == null) {

            throw new RuntimeException(
                    "User not found");
        }

        if (!existingReview
                .getUserId()
                .equals(loggedInUser.getId())) {

            throw new AccessDeniedException(
                    "You can edit only your own review");
        }

        existingReview.setRating(
                request.getRating());

        existingReview.setComment(
                request.getComment());

        existingReview.setDeliveryRating(
                request.getDeliveryRating());

        existingReview.setSupportRating(
                request.getSupportRating());

        existingReview.setRefundRating(
                request.getRefundRating());

        existingReview.setProductRating(
                request.getProductRating());

        existingReview.setPricingRating(
                request.getPricingRating());

        Review savedReview =
                reviewRepository.save(
                        existingReview);

        return convertToResponseDTO(
                savedReview);
    }

    // ==============================
    // DELETE REVIEW
    // ==============================

    @Transactional
    public void deleteReview(Long id) {

        Review existingReview =
                reviewRepository
                        .findById(id)
                        .orElse(null);

        if (existingReview == null) {

            throw new RuntimeException(
                    "Review not found");
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String loggedInEmail =
                authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(
                        loggedInEmail);

        if (loggedInUser == null) {

            throw new RuntimeException(
                    "User not found");
        }

        boolean isAdmin =
                adminRepository.existsByUserId(
                        loggedInUser.getId());

        boolean isOwner =
                existingReview
                        .getUserId()
                        .equals(loggedInUser.getId());

        if (!isOwner && !isAdmin) {

            throw new AccessDeniedException(
                    "You can delete only your own review");
        }

        businessResponseRepository
                .deleteByReviewId(id);

        reviewVoteRepository
                .deleteByReviewId(id);

        reportRepository
                .deleteByReviewId(id);

        reviewRepository.deleteById(id);
    }

    // ==============================
    // DELETE REVIEWS BY WEBSITE
    // ==============================

    @Transactional
    public void deleteReviewsByWebsiteId(
            Long websiteId) {

        List<Review> reviews =
                reviewRepository.findByWebsiteId(
                        websiteId);

        for (Review review :
                reviews) {

            Long reviewId =
                    review.getId();

            businessResponseRepository
                    .deleteByReviewId(
                            reviewId);

            reviewVoteRepository
                    .deleteByReviewId(
                            reviewId);

            reportRepository
                    .deleteByReviewId(
                            reviewId);

            reviewRepository.deleteById(
                    reviewId);
        }
    }

    // ==============================
    // UPDATE REVIEW STATUS
    // ==============================

    @Transactional
    public ReviewResponseDTO updateReviewStatus(
            Long id,
            String status) {

        Review review =
                reviewRepository
                        .findById(id)
                        .orElse(null);

        if (review == null) {
            return null;
        }

        if (status == null
                || status.isBlank()) {

            throw new RuntimeException(
                    "Status is required");
        }

        status =
                status.toUpperCase();

        if (!status.equals("PENDING")
                && !status.equals("APPROVED")
                && !status.equals("REJECTED")
                && !status.equals("HIDDEN")) {

            throw new RuntimeException(
                    "Status must be PENDING, APPROVED, "
                    + "REJECTED or HIDDEN");
        }

        String oldStatus =
                review.getStatus();

        if ("REJECTED".equals(status)) {

            ReviewResponseDTO responseDTO =
                    convertToResponseDTO(
                            review);

            businessResponseRepository
                    .deleteByReviewId(id);

            reviewVoteRepository
                    .deleteByReviewId(id);

            reportRepository
                    .deleteByReviewId(id);

            reviewRepository.deleteById(id);

            auditLogService.log(
                    "REVIEW_STATUS_CHANGED",
                    "REVIEW",
                    id,
                    oldStatus
                            + " -> REJECTED (DELETED)");

            return responseDTO;
        }

        review.setStatus(status);

        Review savedReview =
                reviewRepository.save(
                        review);

        auditLogService.log(
                "REVIEW_STATUS_CHANGED",
                "REVIEW",
                id,
                oldStatus
                        + " -> "
                        + status);

        return convertToResponseDTO(
                savedReview);
    }

    // ==============================
    // UPDATE VERIFICATION STATUS
    // ==============================

    public ReviewResponseDTO
    updateReviewVerificationStatus(
            Long id,
            String verificationStatus) {

        Review review =
                reviewRepository
                        .findById(id)
                        .orElse(null);

        if (review == null) {
            return null;
        }

        if (verificationStatus == null
                || verificationStatus.isBlank()) {

            throw new RuntimeException(
                    "Verification status is required");
        }

        verificationStatus =
                verificationStatus.toUpperCase();

        if (!verificationStatus.equals(
                    "UNVERIFIED")
                && !verificationStatus.equals(
                    "EMAIL_VERIFIED")
                && !verificationStatus.equals(
                    "EXPERIENCE_VERIFIED")
                && !verificationStatus.equals(
                    "UNDER_REVIEW")
                && !verificationStatus.equals(
                    "REMOVED")) {

            throw new RuntimeException(
                    "Verification status must be "
                    + "UNVERIFIED, EMAIL_VERIFIED, "
                    + "EXPERIENCE_VERIFIED, "
                    + "UNDER_REVIEW or REMOVED");
        }

        String oldVerificationStatus =
                review.getVerificationStatus();

        review.setVerificationStatus(
                verificationStatus);

        Review savedReview =
                reviewRepository.save(review);

        auditLogService.log(
                "REVIEW_VERIFICATION_STATUS_CHANGED",
                "REVIEW",
                id,
                oldVerificationStatus
                        + " -> "
                        + verificationStatus);

        return convertToResponseDTO(
                savedReview);
    }

    // ==============================
    // AVERAGE RATING
    // ==============================

    public double getAverageRating(
            Long websiteId) {

        Double average =
                reviewRepository
                        .findAverageCountedRating(
                                websiteId);

        if (average == null) {
            return 0.0;
        }

        return Math.round(
                average * 10.0)
                / 10.0;
    }

    // ==============================
    // REVIEW COUNT
    // ==============================

    public int getReviewCount(
            Long websiteId) {

        Long count =
                reviewRepository
                        .countCountedReviews(
                                websiteId);

        return count == null
                ? 0
                : count.intValue();
    }

    // ==============================
    // FIVE STAR
    // ==============================

    public int getFiveStarCount(
            Long websiteId) {

        return getStarCount(
                websiteId,
                5);
    }

    // ==============================
    // FOUR STAR
    // ==============================

    public int getFourStarCount(
            Long websiteId) {

        return getStarCount(
                websiteId,
                4);
    }

    // ==============================
    // THREE STAR
    // ==============================

    public int getThreeStarCount(
            Long websiteId) {

        return getStarCount(
                websiteId,
                3);
    }

    // ==============================
    // TWO STAR
    // ==============================

    public int getTwoStarCount(
            Long websiteId) {

        return getStarCount(
                websiteId,
                2);
    }

    // ==============================
    // ONE STAR
    // ==============================

    public int getOneStarCount(
            Long websiteId) {

        return getStarCount(
                websiteId,
                1);
    }

    // ==============================
    // STAR COUNT
    // ==============================

    private int getStarCount(
            Long websiteId,
            int star) {

        Long count =
                reviewRepository
                        .countCountedReviewsByRating(
                                websiteId,
                                star);

        return count == null
                ? 0
                : count.intValue();
    }

    // ==============================
    // ENTITY -> DTO
    // ==============================

    public ReviewResponseDTO
    convertToResponseDTO(
            Review review) {

        if (review == null) {
            return null;
        }

        return new ReviewResponseDTO(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getUserId(),
                review.getWebsiteId(),
                review.getStatus(),
                review.getVerificationStatus(),
                review.getDeliveryRating(),
                review.getSupportRating(),
                review.getRefundRating(),
                review.getProductRating(),
                review.getPricingRating(),
                review.getCreatedAt());
    }

    // ==============================
    // LIST ENTITY -> DTO
    // ==============================

    public List<ReviewResponseDTO>
    convertToResponseDTOList(
            List<Review> reviews) {

        return reviews.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}