
package com.nit.dto;

public class BusinessResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String officialUrl;
    private String status;
    private String businessEmail;
    private boolean emailVerified;
    private boolean metaVerified;
    private boolean dnsVerified;

    public BusinessResponseDTO() {
    }

    public BusinessResponseDTO(
            Long id,
            String name,
            String description,
            String officialUrl,
            String status,
            String businessEmail,
            boolean emailVerified,
            boolean metaVerified,
            boolean dnsVerified) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.officialUrl = officialUrl;
        this.status = status;
        this.businessEmail = businessEmail;
        this.emailVerified = emailVerified;
        this.metaVerified = metaVerified;
        this.dnsVerified = dnsVerified;
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

    public boolean isMetaVerified() {
        return metaVerified;
    }

    public void setMetaVerified(boolean metaVerified) {
        this.metaVerified = metaVerified;
    }

    public boolean isDnsVerified() {
        return dnsVerified;
    }

    public void setDnsVerified(boolean dnsVerified) {
        this.dnsVerified = dnsVerified;
    }
}