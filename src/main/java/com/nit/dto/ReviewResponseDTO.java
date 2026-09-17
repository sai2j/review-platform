
package com.nit.dto;

import java.time.LocalDateTime;

public class ReviewResponseDTO {

    private Long id;

    private Integer rating;

    private String comment;

    private Long userId;

    private Long websiteId;

    private String status;

    private String verificationStatus;

    private Integer deliveryRating;

    private Integer supportRating;

    private Integer refundRating;

    private Integer productRating;

    private Integer pricingRating;

    private LocalDateTime createdAt;

    public ReviewResponseDTO() {
    }

    public ReviewResponseDTO(
            Long id,
            Integer rating,
            String comment,
            Long userId,
            Long websiteId,
            String status,
            String verificationStatus,
            Integer deliveryRating,
            Integer supportRating,
            Integer refundRating,
            Integer productRating,
            Integer pricingRating,
            LocalDateTime createdAt) {

        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.userId = userId;
        this.websiteId = websiteId;
        this.status = status;
        this.verificationStatus = verificationStatus;
        this.deliveryRating = deliveryRating;
        this.supportRating = supportRating;
        this.refundRating = refundRating;
        this.productRating = productRating;
        this.pricingRating = pricingRating;
        this.createdAt = createdAt;
    }

    // Old constructor preserved
    public ReviewResponseDTO(
            Long id,
            Integer rating,
            String comment,
            Long userId,
            Long websiteId,
            String status,
            String verificationStatus,
            LocalDateTime createdAt) {

        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.userId = userId;
        this.websiteId = websiteId;
        this.status = status;
        this.verificationStatus = verificationStatus;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

