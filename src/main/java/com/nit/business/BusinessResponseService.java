package com.nit.business;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nit.review.ReviewRepository;

@Service
public class BusinessResponseService {

    private final BusinessResponseRepositor businessResponseRepository;
    private final BusinessRepository businessRepository;
    private final ReviewRepository reviewRepository;

    public BusinessResponseService(
            BusinessResponseRepositor businessResponseRepository,
            BusinessRepository businessRepository,
            ReviewRepository reviewRepository) {

        this.businessResponseRepository = businessResponseRepository;
        this.businessRepository = businessRepository;
        this.reviewRepository = reviewRepository;
    }

    public BusinessResponse saveBusinessResponse(
            BusinessResponse businessResponse) {

        if (!businessRepository.existsById(
                businessResponse.getBusinessId())) {

            throw new RuntimeException(
                    "Business does not exist");
        }

        if (!reviewRepository.existsById(
                businessResponse.getReviewId())) {

            throw new RuntimeException(
                    "Review does not exist");
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

        businessResponseRepository.deleteById(id);
    }
}