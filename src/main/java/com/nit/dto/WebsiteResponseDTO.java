package com.nit.dto;

public class WebsiteResponseDTO {

    private Long id;
    private String name;
    private String url;
    private String description;
    private String canonicalDomain;
    private String seoTitle;
    private String seoDescription;
    private String canonicalUrl;

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
}