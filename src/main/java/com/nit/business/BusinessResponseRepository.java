package com.nit.business;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessResponseRepository
        extends JpaRepository<BusinessResponse, Long> {

    Optional<BusinessResponse> findByBusinessIdAndReviewId(
            Long businessId,
            Long reviewId
    );

    void deleteByReviewId(Long reviewId);
}