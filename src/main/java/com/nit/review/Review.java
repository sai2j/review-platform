
package com.nit.review;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")
    private Integer rating;

    @NotBlank(message = "Comment is required")
    @Column(name = "review_comment")
    private String comment;

    private Long userId;

    private Long websiteId;

    // Moderation status
    private String status;

    // Review verification status
    @Column(name = "verification_status")
    private String verificationStatus;

    // Experience ratings
    @Column(name = "delivery_rating")
    private Integer deliveryRating;

    @Column(name = "support_rating")
    private Integer supportRating;

    @Column(name = "refund_rating")
    private Integer refundRating;

    @Column(name = "product_rating")
    private Integer productRating;

    @Column(name = "pricing_rating")
    private Integer pricingRating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Review() {
    }

    public Review(
            Integer rating,
            String comment,
            Long userId,
            Long websiteId) {

        this.rating = rating;
        this.comment = comment;
        this.userId = userId;
        this.websiteId = websiteId;

        this.status = "PENDING";
        this.verificationStatus = "UNVERIFIED";
    }

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null || status.isBlank()) {
            status = "PENDING";
        }

        if (verificationStatus == null
                || verificationStatus.isBlank()) {

            verificationStatus = "UNVERIFIED";
        }
    }

    public Long getId() {
        return id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getWebsiteId() {
        return websiteId;
    }

    public void setWebsiteId(Long websiteId) {
        this.websiteId = websiteId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Integer getDeliveryRating() {
        return deliveryRating;
    }

    public void setDeliveryRating(Integer deliveryRating) {
        this.deliveryRating = deliveryRating;
    }

    public Integer getSupportRating() {
        return supportRating;
    }

    public void setSupportRating(Integer supportRating) {
        this.supportRating = supportRating;
    }

    public Integer getRefundRating() {
        return refundRating;
    }

    public void setRefundRating(Integer refundRating) {
        this.refundRating = refundRating;
    }

    public Integer getProductRating() {
        return productRating;
    }

    public void setProductRating(Integer productRating) {
        this.productRating = productRating;
    }

    public Integer getPricingRating() {
        return pricingRating;
    }

    public void setPricingRating(Integer pricingRating) {
        this.pricingRating = pricingRating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

