package com.nit.business;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessResponseRepository extends JpaRepository<BusinessResponse, Long> {

	Optional<BusinessResponse> findByBusinessIdAndReviewId(Long businessId, Long reviewId);

	List<BusinessResponse> findByBusinessId(Long businessId);

	void deleteByReviewId(Long reviewId);
}