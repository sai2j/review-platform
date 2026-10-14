package com.nit.dto;

public class WebsiteRankingDTO {

    private Long websiteId;
    private Double averageRating;
    private Long reviewCount;
    private Integer rank;

    public WebsiteRankingDTO() {
    }

    public WebsiteRankingDTO(
            Long websiteId,
            Double averageRating,
            Long reviewCount,
            Integer rank) {

        this.websiteId = websiteId;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.rank = rank;
    }

    public Long getWebsiteId() {
        return websiteId;
    }

    public void setWebsiteId(Long websiteId) {
        this.websiteId = websiteId;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}