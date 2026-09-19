package com.nit.business;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "businesses")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotBlank(message = "Business name is required")
    @Size(max = 200, message = "Business name must not exceed 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Column(name = "official_url")
    @NotBlank(message = "Official URL is required")
    @Size(max = 500, message = "Official URL must not exceed 500 characters")
    private String officialUrl;

    private String status;

    @Column(name = "business_email")
    @Email(message = "Invalid business email")
    @Size(max = 255, message = "Business email must not exceed 255 characters")
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

    @JsonIgnore
    @Column(name = "meta_verification_token")
    private String metaVerificationToken;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "meta_verified")
    private boolean metaVerified = false;

    @JsonIgnore
    @Column(name = "dns_verification_token")
    private String dnsVerificationToken;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "dns_verified")
    private boolean dnsVerified = false;

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

    public void setEmailVerificationExpiry(LocalDateTime emailVerificationExpiry) {
        this.emailVerificationExpiry = emailVerificationExpiry;
    }

    public String getMetaVerificationToken() {
        return metaVerificationToken;
    }

    public void setMetaVerificationToken(String metaVerificationToken) {
        this.metaVerificationToken = metaVerificationToken;
    }

    public boolean isMetaVerified() {
        return metaVerified;
    }

    public void setMetaVerified(boolean metaVerified) {
        this.metaVerified = metaVerified;
    }

    public String getDnsVerificationToken() {
        return dnsVerificationToken;
    }

    public void setDnsVerificationToken(String dnsVerificationToken) {
        this.dnsVerificationToken = dnsVerificationToken;
    }

    public boolean isDnsVerified() {
        return dnsVerified;
    }

    public void setDnsVerified(boolean dnsVerified) {
        this.dnsVerified = dnsVerified;
    }
}