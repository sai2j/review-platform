package com.nit.business;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "business_responses")
public class BusinessResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotNull(message = "Review ID is required")
    private Long reviewId;

    @NotBlank(message = "Response is required")
    private String response;

    public BusinessResponse() {
    }

    public BusinessResponse(Long businessId, Long reviewId, String response) {
        this.businessId = businessId;
        this.reviewId = reviewId;
        this.response = response;
    }

    public Long getId() {
        return id;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}