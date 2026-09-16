package com.nit.business;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nit.review.ReviewRepository;
import com.nit.user.User;
import com.nit.user.UserRepository;

@Service
public class BusinessResponseService {

    private final BusinessResponseRepository businessResponseRepository;
    private final BusinessRepository businessRepository;
    private final ReviewRepository reviewRepository;
    private final BusinessClamService businessClaimService;
    private final UserRepository userRepository;

    public BusinessResponseService(
            BusinessResponseRepository businessResponseRepository,
            BusinessRepository businessRepository,
            ReviewRepository reviewRepository,
            BusinessClamService businessClaimService,
            UserRepository userRepository) {

        this.businessResponseRepository = businessResponseRepository;
        this.businessRepository = businessRepository;
        this.reviewRepository = reviewRepository;
        this.businessClaimService = businessClaimService;
        this.userRepository = userRepository;
    }

    public BusinessResponse saveBusinessResponse(
            BusinessResponse businessResponse) {

        if (!canAccessBusiness(businessResponse.getBusinessId())) {
            throw new AccessDeniedException(
                    "You can respond only for your approved business");
        }

        if (businessResponse.getBusinessId() == null
                || !businessRepository.existsById(
                        businessResponse.getBusinessId())) {

            throw new RuntimeException("Business does not exist");
        }

        if (businessResponse.getReviewId() == null
                || !reviewRepository.existsById(
                        businessResponse.getReviewId())) {

            throw new RuntimeException("Review does not exist");
        }

        BusinessResponse existingResponse =
                businessResponseRepository
                        .findByBusinessIdAndReviewId(
                                businessResponse.getBusinessId(),
                                businessResponse.getReviewId())
                        .orElse(null);

        if (existingResponse != null) {

            existingResponse.setResponse(
                    businessResponse.getResponse());

            return businessResponseRepository.save(
                    existingResponse);
        }

        return businessResponseRepository.save(
                businessResponse);
    }

    public List<BusinessResponse> getAllBusinessResponses() {
        return businessResponseRepository.findAll();
    }

    public BusinessResponse getBusinessResponseById(Long id) {
        return businessResponseRepository
                .findById(id)
                .orElse(null);
    }

    public BusinessResponse updateBusinessResponse(
            Long id,
            BusinessResponse businessResponse) {

        if (!canAccessResponse(id)) {
            throw new AccessDeniedException(
                    "You can update only your business response");
        }

        BusinessResponse existingResponse =
                businessResponseRepository
                        .findById(id)
                        .orElse(null);

        if (existingResponse == null) {
            return null;
        }

        existingResponse.setResponse(
                businessResponse.getResponse());

        return businessResponseRepository.save(
                existingResponse);
    }

    public void deleteBusinessResponse(Long id) {

        if (!canAccessResponse(id)) {
            throw new AccessDeniedException(
                    "You can delete only your business response");
        }

        businessResponseRepository.deleteById(id);
    }

    public boolean canAccessBusiness(Long businessId) {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return false;
        }

        User loggedInUser =
                userRepository.findByEmail(
                        authentication.getName());

        if (loggedInUser == null) {
            return false;
        }

        BusinessClaim claim =
                businessClaimService
                        .getApprovedClaimByUserId(
                                loggedInUser.getId());

        if (claim == null) {
            return false;
        }

        return claim.getBusinessId().equals(businessId);
    }

    public boolean canAccessResponse(Long responseId) {

        BusinessResponse response =
                businessResponseRepository
                        .findById(responseId)
                        .orElse(null);

        if (response == null) {
            return false;
        }

        return canAccessBusiness(
                response.getBusinessId());
    }
}