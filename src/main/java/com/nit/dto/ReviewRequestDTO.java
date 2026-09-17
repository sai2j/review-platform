
package com.nit.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewRequestDTO {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @NotBlank(message = "Comment is required")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;

    @NotNull(message = "Website ID is required")
    private Long websiteId;

    // Experience ratings
    @Min(value = 1, message = "Delivery rating must be between 1 and 5")
    @Max(value = 5, message = "Delivery rating must be between 1 and 5")
    private Integer deliveryRating;

    @Min(value = 1, message = "Support rating must be between 1 and 5")
    @Max(value = 5, message = "Support rating must be between 1 and 5")
    private Integer supportRating;

    @Min(value = 1, message = "Refund rating must be between 1 and 5")
    @Max(value = 5, message = "Refund rating must be between 1 and 5")
    private Integer refundRating;

    @Min(value = 1, message = "Product rating must be between 1 and 5")
    @Max(value = 5, message = "Product rating must be between 1 and 5")
    private Integer productRating;

    @Min(value = 1, message = "Pricing rating must be between 1 and 5")
    @Max(value = 5, message = "Pricing rating must be between 1 and 5")
    private Integer pricingRating;

    public ReviewRequestDTO() {
    }

    public ReviewRequestDTO(
            Integer rating,
            String comment,
            Long websiteId) {

        this.rating = rating;
        this.comment = comment;
        this.websiteId = websiteId;
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

    public Long getWebsiteId() {
        return websiteId;
    }

    public void setWebsiteId(Long websiteId) {
        this.websiteId = websiteId;
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
}

