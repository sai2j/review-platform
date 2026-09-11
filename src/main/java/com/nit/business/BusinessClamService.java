package com.nit.business;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class BusinessClamService {

    private final BusinessclaimRepository businessClaimRepository;

    public BusinessClamService(BusinessclaimRepository businessClaimRepository) {
        this.businessClaimRepository = businessClaimRepository;
    }

    public BusinessClaim saveBusinessClaim(BusinessClaim businessClaim) {
        return businessClaimRepository.save(businessClaim);
    }

    public List<BusinessClaim> getAllBusinessClaims() {
        return businessClaimRepository.findAll();
    }

    public BusinessClaim getBusinessClaimById(Long id) {
        return businessClaimRepository.findById(id).orElse(null);
    }

    public BusinessClaim updateClaimStatus(Long id, String status) {
        BusinessClaim claim = businessClaimRepository.findById(id).orElse(null);

        if (claim == null) {
            return null;
        }

        claim.setStatus(status);
        return businessClaimRepository.save(claim);
    }

    public BusinessClaim getApprovedClaimByUserId(Long userId) {

        Optional<BusinessClaim> claim =
                businessClaimRepository.findByUserIdAndStatus(userId, "APPROVED");

        return claim.orElse(null);
    }

    public void deleteBusinessClaim(Long id) {
        businessClaimRepository.deleteById(id);
    }
}