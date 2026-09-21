package com.nit.review;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	List<Review> findByWebsiteId(Long websiteId);
	Page<Review> findByWebsiteId(Long websiteId, Pageable pageable);
	Page<Review> findByWebsiteIdAndRating(Long websiteId, Integer rating, Pageable pageable);
	Page<Review> findByWebsiteIdAndVerificationStatus(Long websiteId, String verificationStatus, Pageable pageable);
	Page<Review> findByWebsiteIdAndDeliveryRating(Long websiteId, Integer deliveryRating, Pageable pageable);
	Page<Review> findByWebsiteIdAndSupportRating(Long websiteId, Integer supportRating, Pageable pageable);
	Page<Review> findByWebsiteIdAndRefundRating(Long websiteId, Integer refundRating, Pageable pageable);
	Page<Review> findByWebsiteIdAndProductRating(Long websiteId, Integer productRating, Pageable pageable);
	Page<Review> findByWebsiteIdAndPricingRating(Long websiteId, Integer pricingRating, Pageable pageable);
	Page<Review> findByWebsiteIdAndCreatedAtBetween(Long websiteId, LocalDateTime startDateTime,
			LocalDateTime endDateTime, Pageable pageable);
	List<Review> findByStatus(String status);
	Page<Review> findByWebsiteIdAndStatusIgnoreCase(Long websiteId, String status, Pageable pageable);
	boolean existsByUserIdAndWebsiteIdAndComment(Long userId, Long websiteId, String comment);
	long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime createdAt);
	@Query("""
			    SELECT AVG(r.rating)
			    FROM Review r
			    WHERE r.websiteId = :websiteId
			    AND UPPER(r.status) = 'APPROVED'
			""")
	Double findAverageApprovedRating(@Param("websiteId") Long websiteId);
	@Query("""
			    SELECT COUNT(r)
			    FROM Review r
			    WHERE r.websiteId = :websiteId
			    AND UPPER(r.status) = 'APPROVED'
			""")
	Long countApprovedReviews(@Param("websiteId") Long websiteId);
	@Query("""
			    SELECT COUNT(r)
			    FROM Review r
			    WHERE r.websiteId = :websiteId
			    AND UPPER(r.status) = 'APPROVED'
			    AND r.rating = :rating
			""")
	Long countApprovedReviewsByRating(@Param("websiteId") Long websiteId, @Param("rating") Integer rating);
}