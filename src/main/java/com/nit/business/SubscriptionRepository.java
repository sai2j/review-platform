package com.nit.business;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

	Optional<Subscription> findTopByBusinessIdOrderByIdDesc(Long businessId);

	Optional<Subscription> findTopByBusinessIdAndStatusOrderByIdDesc(Long businessId, String status);

	List<Subscription> findByStatusAndEndDateBefore(String status, LocalDateTime dateTime);

	List<Subscription> findByBusinessIdAndStatus(Long businessId, String status);
}