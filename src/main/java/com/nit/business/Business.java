package com.nit.business;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;

@Entity
@Table(name = "businesses")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    private String description;

    @Column(name = "official_url")
    private String officialUrl;

    private String status;

    // ==============================
    // BUSINESS EMAIL VERIFICATION
    // ==============================

    @Column(name = "business_email")
    private String businessEmail;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @JsonIgnore
    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @JsonIgnore
    @Column(name = "email_verification_expiry")
    private LocalDateTime emailVerificationExpiry;

    // ==============================
    // META TAG VERIFICATION
    // ==============================

    @JsonIgnore
    @Column(name = "meta_verification_token")
    private String metaVerificationToken;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "meta_verified")
    private boolean metaVerified = false;

    public Business() {
    }

    public Business(String name, String description,
                    String officialUrl, String status) {

        this.name = name;
        this.description = description;
        this.officialUrl = officialUrl;
        this.status = status;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOfficialUrl() {
        return officialUrl;
    }

    public void setOfficialUrl(String officialUrl) {
        this.officialUrl = officialUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ==============================
    // BUSINESS EMAIL GETTERS/SETTERS
    // ==============================

    public String getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(String businessEmail) {
        this.businessEmail = businessEmail;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }

    public LocalDateTime getEmailVerificationExpiry() {
        return emailVerificationExpiry;
    }

    public void setEmailVerificationExpiry(
            LocalDateTime emailVerificationExpiry) {

        this.emailVerificationExpiry =
                emailVerificationExpiry;
    }

    // ==============================
    // META TAG GETTERS/SETTERS
    // ==============================

    public String getMetaVerificationToken() {
        return metaVerificationToken;
    }

    public void setMetaVerificationToken(
            String metaVerificationToken) {

        this.metaVerificationToken =
                metaVerificationToken;
    }

    public boolean isMetaVerified() {
        return metaVerified;
    }

    public void setMetaVerified(boolean metaVerified) {
        this.metaVerified = metaVerified;
    }
}