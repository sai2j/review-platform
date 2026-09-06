package com.nit.review;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private Integer rating;
    @Column(name = "review_comment")
    private String comment;
    private Long userId;
    private Long websiteId;
    public Review() {
    }
    public Review(Integer rating, String comment, Long userId, Long websiteId) {
        this.rating = rating;
        this.comment = comment;
        this.userId = userId;
        this.websiteId = websiteId;
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
}