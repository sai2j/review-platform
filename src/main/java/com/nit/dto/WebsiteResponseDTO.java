
package com.nit.dto;

public class WebsiteResponseDTO {

    private Long id;
    private String name;
    private String url;
    private String description;
    private String canonicalDomain;

    // SEO
    private String seoTitle;
    private String seoDescription;
    private String canonicalUrl;

    // Website Profile
    private boolean claimed;
    private boolean verified;

    private double averageRating;
    private int reviewCount;

    private int fiveStarCount;
    private int fourStarCount;
    private int threeStarCount;
    private int twoStarCount;
    private int oneStarCount;

    public WebsiteResponseDTO() {
    }

    public WebsiteResponseDTO(
            Long id,
            String name,
            String url,
            String description,
            String canonicalDomain,
            String seoTitle,
            String seoDescription,
            String canonicalUrl) {

        this.id = id;
        this.name = name;
        this.url = url;
        this.description = description;
        this.canonicalDomain = canonicalDomain;
        this.seoTitle = seoTitle;
        this.seoDescription = seoDescription;
        this.canonicalUrl = canonicalUrl;
    }

    public WebsiteResponseDTO(
            Long id,
            String name,
            String url,
            String description,
            String canonicalDomain,
            String seoTitle,
            String seoDescription,
            String canonicalUrl,
            boolean claimed,
            boolean verified,
            double averageRating,
            int reviewCount,
            int fiveStarCount,
            int fourStarCount,
            int threeStarCount,
            int twoStarCount,
            int oneStarCount) {

        this.id = id;
        this.name = name;
        this.url = url;
        this.description = description;
        this.canonicalDomain = canonicalDomain;
        this.seoTitle = seoTitle;
        this.seoDescription = seoDescription;
        this.canonicalUrl = canonicalUrl;

        this.claimed = claimed;
        this.verified = verified;

        this.averageRating = averageRating;
        this.reviewCount = reviewCount;

        this.fiveStarCount = fiveStarCount;
        this.fourStarCount = fourStarCount;
        this.threeStarCount = threeStarCount;
        this.twoStarCount = twoStarCount;
        this.oneStarCount = oneStarCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCanonicalDomain() {
        return canonicalDomain;
    }

    public void setCanonicalDomain(String canonicalDomain) {
        this.canonicalDomain = canonicalDomain;
    }

    public String getSeoTitle() {
        return seoTitle;
    }

    public void setSeoTitle(String seoTitle) {
        this.seoTitle = seoTitle;
    }

    public String getSeoDescription() {
        return seoDescription;
    }

    public void setSeoDescription(String seoDescription) {
        this.seoDescription = seoDescription;
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public void setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public int getFiveStarCount() {
        return fiveStarCount;
    }

    public void setFiveStarCount(int fiveStarCount) {
        this.fiveStarCount = fiveStarCount;
    }

    public int getFourStarCount() {
        return fourStarCount;
    }

    public void setFourStarCount(int fourStarCount) {
        this.fourStarCount = fourStarCount;
    }

    public int getThreeStarCount() {
        return threeStarCount;
    }

    public void setThreeStarCount(int threeStarCount) {
        this.threeStarCount = threeStarCount;
    }

    public int getTwoStarCount() {
        return twoStarCount;
    }

    public void setTwoStarCount(int twoStarCount) {
        this.twoStarCount = twoStarCount;
    }

    public int getOneStarCount() {
        return oneStarCount;
    }

    public void setOneStarCount(int oneStarCount) {
        this.oneStarCount = oneStarCount;
    }
}

