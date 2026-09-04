package com.nit.business;

import jakarta.persistence.*;

@Entity
@Table(name = "business_claims")
public class BusinessClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private Long businessId;

    private Long userId;

    private String status;

    public BusinessClaim() {
    }

    public BusinessClaim(Long businessId, Long userId, String status) {
        this.businessId = businessId;
        this.userId = userId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}