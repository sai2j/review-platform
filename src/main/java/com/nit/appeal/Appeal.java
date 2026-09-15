package com.nit.appeal;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "APPEALS")
public class Appeal {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "appeals_seq_generator"
    )
    @SequenceGenerator(
        name = "appeals_seq_generator",
        sequenceName = "APPEALS_SEQ",
        allocationSize = 1
    )
    private Long id;

    @Column(name = "REVIEW_ID", nullable = false)
    private Long reviewId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "REASON", nullable = false, length = 1000)
    private String reason;

    @Column(name = "STATUS", length = 50)
    private String status;

    @Column(name = "DECISION", length = 1000)
    private String decision;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public Appeal() {
    }

    public Appeal(
            Long reviewId,
            Long userId,
            String reason) {

        this.reviewId = reviewId;
        this.userId = userId;
        this.reason = reason;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}