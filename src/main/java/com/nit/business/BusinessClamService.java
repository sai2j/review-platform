package com.nit.business;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nit.user.User;
import com.nit.user.UserRepository;

@Service
public class BusinessClamService {

    private final BusinessclaimRepository businessClaimRepository;

    private final UserRepository userRepository;

    private final BusinessRepository businessRepository;

    public BusinessClamService(
            BusinessclaimRepository businessClaimRepository,
            UserRepository userRepository,
            BusinessRepository businessRepository) {

        this.businessClaimRepository = businessClaimRepository;
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
    }

    // ==============================
    // CREATE BUSINESS CLAIM
    // ==============================

    public BusinessClaim saveBusinessClaim(
            BusinessClaim businessClaim) {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        String loggedInEmail =
                authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {
            throw new RuntimeException("User not found");
        }

        // Do not trust userId from frontend
        businessClaim.setUserId(loggedInUser.getId());

        // Check business exists
        if (businessClaim.getBusinessId() == null
                || !businessRepository.existsById(
                        businessClaim.getBusinessId())) {

            throw new RuntimeException(
                    "Business does not exist");
        }

        // New claims must always start as PENDING
        businessClaim.setStatus("PENDING");

        return businessClaimRepository.save(businessClaim);
    }

    // ==============================
    // GET ALL BUSINESS CLAIMS
    // ==============================

    public List<BusinessClaim> getAllBusinessClaims() {

        return businessClaimRepository.findAll();
    }

    // ==============================
    // GET BUSINESS CLAIM BY ID
    // ==============================

    public BusinessClaim getBusinessClaimById(Long id) {

        return businessClaimRepository
                .findById(id)
                .orElse(null);
    }

    // ==============================
    // UPDATE CLAIM STATUS
    // ==============================

    public BusinessClaim updateClaimStatus(
            Long id,
            String status) {

        BusinessClaim claim =
                businessClaimRepository
                        .findById(id)
                        .orElse(null);

        if (claim == null) {
            return null;
        }

        claim.setStatus(status);

        return businessClaimRepository.save(claim);
    }

    // ==============================
    // GET APPROVED CLAIM BY USER
    // ==============================

    public BusinessClaim getApprovedClaimByUserId(
            Long userId) {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        String loggedInEmail =
                authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {
            throw new RuntimeException("User not found");
        }

        // User can only access their own approved claim
        if (!loggedInUser.getId().equals(userId)) {

            throw new AccessDeniedException(
                    "You can access only your own business claim");
        }

        Optional<BusinessClaim> claim =
                businessClaimRepository
                        .findByUserIdAndStatus(
                                userId,
                                "APPROVED");

        return claim.orElse(null);
    }

    // ==============================
    // GET APPROVED CLAIM BY BUSINESS
    // ==============================

    public BusinessClaim getApprovedClaimByBusinessId(
            Long businessId) {

        Optional<BusinessClaim> claim =
                businessClaimRepository
                        .findByBusinessIdAndStatus(
                                businessId,
                                "APPROVED");

        return claim.orElse(null);
    }

    // ==============================
    // DELETE BUSINESS CLAIM
    // ==============================

    public void deleteBusinessClaim(Long id) {

        businessClaimRepository.deleteById(id);
    }
}