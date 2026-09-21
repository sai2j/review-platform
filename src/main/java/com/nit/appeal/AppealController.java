package com.nit.appeal;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appeals")
public class AppealController {

	private final AppealService appealService;

	public AppealController(AppealService appealService) {
		this.appealService = appealService;
	}

	@PostMapping
	public Appeal createAppeal(@RequestParam Long reviewId, @RequestParam String reason) {
		return appealService.createAppeal(reviewId, reason);
	}

	@GetMapping("/review/{reviewId}")
	public List<Appeal> getAppealsByReviewId(@PathVariable Long reviewId) {
		return appealService.getAppealsByReviewId(reviewId);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping
	public List<Appeal> getAllAppeals() {
		return appealService.getAllAppeals();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{id}")
	public Appeal getAppealById(@PathVariable Long id) {
		return appealService.getAppealById(id);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{id}/decision")
	public Appeal updateAppealDecision(@PathVariable Long id, @RequestParam String status,
			@RequestParam(required = false) String decision) {
		return appealService.updateAppealStatus(id, status, decision);
	}
}