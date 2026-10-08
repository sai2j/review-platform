
package com.nit.review;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByWebsiteId(Long websiteId);

    Page<Review> findByWebsiteId(Long websiteId, Pageable pageable);

    Page<Review> findByWebsiteIdAndRating(
            Long websiteId,
            Integer rating,
            Pageable pageable);

    Page<Review> findByWebsiteIdAndVerificationStatus(
            Long websiteId,
            String verificationStatus,
            Pageable pageable);

    Page<Review> findByWebsiteIdAndDeliveryRating(
            Long websiteId,
            Integer deliveryRating,
            Pageable pageable);

    Page<Review> findByWebsiteIdAndSupportRating(
            Long websiteId,
            Integer supportRating,
            Pageable pageable);

    Page<Review> findByWebsiteIdAndRefundRating(
            Long websiteId,
            Integer refundRating,
            Pageable pageable);

    Page<Review> findByWebsiteIdAndProductRating(
            Long websiteId,
            Integer productRating,
            Pageable pageable);

    Page<Review> findByWebsiteIdAndPricingRating(
            Long websiteId,
            Integer pricingRating,
            Pageable pageable);

    Page<Review> findByWebsiteIdAndCreatedAtBetween(
            Long websiteId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Pageable pageable);

    List<Review> findByStatus(String status);

    Page<Review> findByWebsiteIdAndStatusIgnoreCase(
            Long websiteId,
            String status,
            Pageable pageable);

    boolean existsByUserIdAndWebsiteIdAndComment(
            Long userId,
            Long websiteId,
            String comment);

    long countByUserIdAndCreatedAtAfter(
            Long userId,
            LocalDateTime createdAt);

    // ============================================
    // WEBSITE RANKING - APPROVED REVIEWS ONLY
    // ============================================

    @Query("""
            SELECT r.websiteId, AVG(r.rating), COUNT(r)
            FROM Review r
            WHERE UPPER(r.status) = 'APPROVED'
            GROUP BY r.websiteId
            ORDER BY AVG(r.rating) DESC, COUNT(r) DESC
            """)
    List<Object[]> findWebsiteRanking();
    // ============================================
    // MOVE REVIEWS DURING WEBSITE MERGE
    // ============================================

    @Modifying
    @Query("""
            UPDATE Review r
            SET r.websiteId = :targetWebsiteId
            WHERE r.websiteId = :sourceWebsiteId
            """)
    int moveReviewsToWebsite(
            @Param("sourceWebsiteId") Long sourceWebsiteId,
            @Param("targetWebsiteId") Long targetWebsiteId);

    // ============================================
    // AVERAGE RATING - APPROVED REVIEWS ONLY
    // ============================================

    @Query("""
            SELECT AVG(r.rating)
            FROM Review r
            WHERE r.websiteId = :websiteId
            AND UPPER(r.status) = 'APPROVED'
            """)
    Double findAverageCountedRating(
            @Param("websiteId") Long websiteId);

    // ============================================
    // TOTAL REVIEW COUNT - APPROVED ONLY
    // ============================================

    @Query("""
            SELECT COUNT(r)
            FROM Review r
            WHERE r.websiteId = :websiteId
            AND UPPER(r.status) = 'APPROVED'
            """)
    Long countCountedReviews(
            @Param("websiteId") Long websiteId);

    // ============================================
    // STAR COUNT - APPROVED REVIEWS ONLY
    // ============================================

    @Query("""
            SELECT COUNT(r)
            FROM Review r
            WHERE r.websiteId = :websiteId
            AND UPPER(r.status) = 'APPROVED'
            AND r.rating = :rating
            """)
    Long countCountedReviewsByRating(
            @Param("websiteId") Long websiteId,
            @Param("rating") Integer rating);

    // ============================================
    // APPROVED REVIEW COUNT
    // ============================================

    @Query("""
            SELECT COUNT(r)
            FROM Review r
            WHERE r.websiteId = :websiteId
            AND UPPER(r.status) = 'APPROVED'
            """)
    Long countApprovedReviews(
            @Param("websiteId") Long websiteId);

    // ============================================
    // PUBLIC FILTERS - APPROVED ONLY
    // ============================================

    Page<Review> findByWebsiteIdAndStatusIgnoreCaseAndRating(
            Long websiteId,
            String status,
            Integer rating,
            Pageable pageable);

    Page<Review> findByWebsiteIdAndStatusIgnoreCaseAndVerificationStatus(
            Long websiteId,
            String status,
            String verificationStatus,
            Pageable pageable);

    Page<Review> findByWebsiteIdAndStatusIgnoreCaseAndDeliveryRating(
            Long websiteId,
            String status,
            Integer deliveryRating,
            Pageable pageable);

    Page<Review> findByWebsiteIdAndStatusIgnoreCaseAndSupportRating(
            Long websiteId,
            String status,
            Integer supportRating,
            Pageable pageable);

    Page<Review> findByWebsiteIdAndStatusIgnoreCaseAndRefundRating(
            Long websiteId,
            String status,
            Integer refundRating,
            Pageable pageable);

    Page<Review> findByWebsiteIdAndStatusIgnoreCaseAndProductRating(
            Long websiteId,
            String status,
            Integer productRating,
            Pageable pageable);

    Page<Review> findByWebsiteIdAndStatusIgnoreCaseAndPricingRating(
            Long websiteId,
            String status,
            Integer pricingRating,
            Pageable pageable);

    Page<Review> findByWebsiteIdAndStatusIgnoreCaseAndCreatedAtBetween(
            Long websiteId,
            String status,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Pageable pageable);
}

