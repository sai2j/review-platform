package com.nit.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nit.Report.ReportRepository;
import com.nit.Website.WebsiteRepository;
import com.nit.admin.AdminRepository;
import com.nit.audit.AuditLogService;
import com.nit.business.BusinessResponseRepository;
import com.nit.dto.ReviewResponseDTO;
import com.nit.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private WebsiteRepository websiteRepository;

	@Mock
	private AdminRepository adminRepository;

	@Mock
	private ReviewVoteRepository reviewVoteRepository;

	@Mock
	private ReportRepository reportRepository;

	@Mock
	private BusinessResponseRepository businessResponseRepository;

	@Mock
	private AuditLogService auditLogService;

	private ReviewService reviewService;

	@BeforeEach
	void setUp() {

		reviewService = new ReviewService(reviewRepository, userRepository, websiteRepository, adminRepository,
				reviewVoteRepository, reportRepository, businessResponseRepository, auditLogService);
	}

	// ==============================
	// RATING LOGIC TESTS
	// ==============================

	@Test
	void shouldCalculateAverageRating() {

		when(reviewRepository.findAverageCountedRating(302L)).thenReturn(4.26);

		double result = reviewService.getAverageRating(302L);

		assertEquals(4.3, result);
	}

	@Test
	void shouldReturnZeroWhenAverageRatingIsNull() {

		when(reviewRepository.findAverageCountedRating(302L)).thenReturn(null);

		double result = reviewService.getAverageRating(302L);

		assertEquals(0.0, result);
	}

	@Test
	void shouldRoundAverageRatingToOneDecimal() {

		when(reviewRepository.findAverageCountedRating(302L)).thenReturn(3.24);

		double result = reviewService.getAverageRating(302L);

		assertEquals(3.2, result);
	}

	// ==============================
	// REVIEW COUNT TEST
	// ==============================

	@Test
	void shouldReturnReviewCount() {

		when(reviewRepository.countCountedReviews(302L)).thenReturn(3L);

		int result = reviewService.getReviewCount(302L);

		assertEquals(3, result);
	}

	// ==============================
	// MODERATION TESTS
	// ==============================

	@Test
	void shouldApproveReview() {

		Review review = new Review();

		review.setStatus("PENDING");

		when(reviewRepository.findById(1L)).thenReturn(java.util.Optional.of(review));

		when(reviewRepository.save(review)).thenReturn(review);

		ReviewResponseDTO result = reviewService.updateReviewStatus(1L, "APPROVED");

		assertEquals("APPROVED", result.getStatus());
	}

	@Test
	void shouldRejectReview() {

		Review review = new Review();

		review.setStatus("PENDING");

		when(reviewRepository.findById(2L)).thenReturn(java.util.Optional.of(review));

		ReviewResponseDTO result = reviewService.updateReviewStatus(2L, "REJECTED");

		assertEquals("REJECTED", result.getStatus());
	}

	@Test
	void shouldHideReview() {

		Review review = new Review();

		review.setStatus("APPROVED");

		when(reviewRepository.findById(3L)).thenReturn(java.util.Optional.of(review));

		when(reviewRepository.save(review)).thenReturn(review);

		ReviewResponseDTO result = reviewService.updateReviewStatus(3L, "HIDDEN");

		assertEquals("HIDDEN", result.getStatus());
	}

	@Test
	void shouldRestoreHiddenReview() {

		Review review = new Review();

		review.setStatus("HIDDEN");

		when(reviewRepository.findById(4L)).thenReturn(java.util.Optional.of(review));

		when(reviewRepository.save(review)).thenReturn(review);

		ReviewResponseDTO result = reviewService.updateReviewStatus(4L, "APPROVED");

		assertEquals("APPROVED", result.getStatus());
	}

	@Test
	void shouldRejectInvalidModerationStatus() {

		Review review = new Review();

		review.setStatus("PENDING");

		when(reviewRepository.findById(5L)).thenReturn(java.util.Optional.of(review));

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> reviewService.updateReviewStatus(5L, "DELETED"));

		assertEquals("Status must be PENDING, APPROVED, REJECTED or HIDDEN", exception.getMessage());
	}

	@Test
	void shouldRejectBlankModerationStatus() {

		Review review = new Review();

		review.setStatus("PENDING");

		when(reviewRepository.findById(6L)).thenReturn(java.util.Optional.of(review));

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> reviewService.updateReviewStatus(6L, " "));

		assertEquals("Status is required", exception.getMessage());
	}

	@Test
	void shouldRejectNullModerationStatus() {

		Review review = new Review();

		review.setStatus("PENDING");

		when(reviewRepository.findById(7L)).thenReturn(java.util.Optional.of(review));

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> reviewService.updateReviewStatus(7L, null));

		assertEquals("Status is required", exception.getMessage());
	}
}