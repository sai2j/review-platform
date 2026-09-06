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
	public List<Review> getReviewsByWebsiteId(Long websiteId) {
	    return reviewRepository.findByWebsiteId(websiteId);
	}
	public List<Review> getAllReviews() {
		return reviewRepository.findAll();
	}
	public Review getReviewById(Long id) {
		return reviewRepository.findById(id).orElse(null);
	}
	public Review updateReview(Long id, Review review) {
	    Review existingReview = reviewRepository.findById(id).orElse(null);
	    if (existingReview == null) {
	        return null;
	    }
	    existingReview.setRating(review.getRating());
	    existingReview.setComment(review.getComment());
	    return reviewRepository.save(existingReview);
	}
	public void deleteReview(Long id) {
		reviewRepository.deleteById(id);
	}
}
