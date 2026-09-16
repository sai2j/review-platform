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

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getWebsiteId() {
        return websiteId;
    }

    public String getStatus() {
        return status;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setWebsiteId(Long websiteId) {
        this.websiteId = websiteId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}