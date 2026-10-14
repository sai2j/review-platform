package com.nit.business;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

	private final SubscriptionRepository subscriptionRepository;
	public SubscriptionService(SubscriptionRepository subscriptionRepository) {
		this.subscriptionRepository = subscriptionRepository;
	}
	public Optional<Subscription> getSubscriptionByBusinessId(Long businessId) {
		Optional<Subscription> optionalSubscription = subscriptionRepository
				.findTopByBusinessIdAndStatusOrderByIdDesc(businessId, "ACTIVE");
		if (optionalSubscription.isEmpty()) {
			return Optional.empty();
		}
		Subscription subscription = optionalSubscription.get();
		if (subscription.getEndDate() != null && !subscription.getEndDate().isAfter(LocalDateTime.now())) {
			subscription.setStatus("EXPIRED");
			subscriptionRepository.save(subscription);
			return Optional.empty();
		}
		return Optional.of(subscription);
	}
	public Subscription saveSubscription(Subscription subscription) {
		return subscriptionRepository.save(subscription);
	}
	public Optional<Subscription> processTestPayment(Long subscriptionId) {
		Optional<Subscription> optionalSubscription = subscriptionRepository.findById(subscriptionId);
		if (optionalSubscription.isEmpty()) {
			return Optional.empty();
		}
		Subscription subscription = optionalSubscription.get();
		List<Subscription> activeSubscriptions = subscriptionRepository
				.findByBusinessIdAndStatus(subscription.getBusinessId(), "ACTIVE");
		for (Subscription activeSubscription : activeSubscriptions) {
			if (!activeSubscription.getId().equals(subscription.getId())) {
				return Optional.empty();
			}
		}
		subscription.setStatus("ACTIVE");
		subscription.setPaymentId("TEST_PAYMENT_" + UUID.randomUUID());
		LocalDateTime now = LocalDateTime.now();
		subscription.setStartDate(now);
		if ("MONTHLY".equalsIgnoreCase(subscription.getBillingCycle())) {
			subscription.setEndDate(now.plusMonths(1));
		} else if ("YEARLY".equalsIgnoreCase(subscription.getBillingCycle())) {
			subscription.setEndDate(now.plusYears(1));
		}
		return Optional.of(subscriptionRepository.save(subscription));
	}
	@Scheduled(fixedRate = 60000)
	public void expireSubscriptions() {
		LocalDateTime now = LocalDateTime.now();
		List<Subscription> expiredSubscriptions = subscriptionRepository.findByStatusAndEndDateBefore("ACTIVE", now);
		if (expiredSubscriptions.isEmpty()) {
			return;
		}
		for (Subscription subscription : expiredSubscriptions) {
			subscription.setStatus("EXPIRED");
		}
		subscriptionRepository.saveAll(expiredSubscriptions);
	}
}