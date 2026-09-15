package com.nit.complaint;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "COMPLAINTS")
public class Complaint {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "complaints_seq_generator"
    )
    @SequenceGenerator(
        name = "complaints_seq_generator",
        sequenceName = "COMPLAINTS_SEQ",
        allocationSize = 1
    )
    private Long id;

    @Column(name = "REVIEW_ID", nullable = false)
    private Long reviewId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "TOPIC", nullable = false, length = 100)
    private String topic;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "STATUS", length = 50)
    private String status;

    @Column(name = "RESOLUTION", length = 1000)
    private String resolution;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public Complaint() {
    }

    public Complaint(
            Long reviewId,
            Long userId,
            String topic,
            String description) {

        this.reviewId = reviewId;
        this.userId = userId;
        this.topic = topic;
        this.description = description;
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}