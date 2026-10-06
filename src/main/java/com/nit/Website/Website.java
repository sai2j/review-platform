
package com.nit.Website;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "websites")
public class Website {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotBlank(message = "Website name is required")
    private String name;

    @NotBlank(message = "Website URL is required")
    private String url;

    private String description;
    private String canonicalDomain;
    private String seoTitle;
    private String seoDescription;
    private String canonicalUrl;

    // New fields
    private String category;
    private String country;

    public Website() {
    }

    public Website(String name, String url, String description) {
        this.name = name;
        this.url = url;
        this.description = description;
    }

    public Long getId() {
        return id;
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

    // Category getter and setter
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Country getter and setter
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}

