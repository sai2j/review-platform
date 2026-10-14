package com.nit.business;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {
	private final SubscriptionService subscriptionService;
	public SubscriptionController(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	@GetMapping("/business/{businessId}")
	public ResponseEntity<?> getSubscription(@PathVariable Long businessId) {
		Optional<Subscription> subscription = subscriptionService.getSubscriptionByBusinessId(businessId);
		if (subscription.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(subscription.get());
	}
	@PostMapping("/business/{businessId}/create")
	public ResponseEntity<?> createSubscription(@PathVariable Long businessId, @RequestParam String planName,
			@RequestParam String billingCycle) {
		BigDecimal amount;
		if ("Starter".equalsIgnoreCase(planName)) {
			amount = "YEARLY".equalsIgnoreCase(billingCycle) ? new BigDecimal("799") : new BigDecimal("999");
		} else if ("Professional".equalsIgnoreCase(planName)) {
			amount = "YEARLY".equalsIgnoreCase(billingCycle) ? new BigDecimal("2399") : new BigDecimal("2999");
		} else if ("Premium".equalsIgnoreCase(planName)) {
			amount = "YEARLY".equalsIgnoreCase(billingCycle) ? new BigDecimal("5599") : new BigDecimal("6999");
		} else {
			return ResponseEntity.badRequest().body("Invalid plan name");
		}
		if (!"MONTHLY".equalsIgnoreCase(billingCycle) && !"YEARLY".equalsIgnoreCase(billingCycle)) {
			return ResponseEntity.badRequest().body("Invalid billing cycle");
		}
		Subscription subscription = new Subscription();
		subscription.setBusinessId(businessId);
		subscription.setPlanName(planName);
		subscription.setBillingCycle(billingCycle.toUpperCase());
		subscription.setAmount(amount);
		subscription.setStatus("PENDING_PAYMENT");
		subscription.setStartDate(LocalDateTime.now());
		Subscription savedSubscription = subscriptionService.saveSubscription(subscription);
		return ResponseEntity.ok(savedSubscription);
	}
	@PostMapping("/{subscriptionId}/test-payment")
	public ResponseEntity<?> processTestPayment(@PathVariable Long subscriptionId) {
		Optional<Subscription> subscription = subscriptionService.processTestPayment(subscriptionId);
		if (subscription.isEmpty()) {
			return ResponseEntity.status(409).body("Another subscription is already active for this business.");
		}
		return ResponseEntity.ok(subscription.get());
	}
}