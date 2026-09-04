package com.nit.review;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ReviewService {

	public final ReviewRepository reviewRepository;

	public ReviewService(ReviewRepository reviewRepository) {
		this.reviewRepository = reviewRepository;
	}

	public Review saveReview(Review review) {
		return reviewRepository.save(review);
	}

	public List<Review> getAllReviews() {
		return reviewRepository.findAll();
	}

	public Review getReviewById(Long id) {
		return reviewRepository.findById(id).orElse(null);
	}

	public void deleteReview(Long id) {
		reviewRepository.deleteById(id);
	}
}
