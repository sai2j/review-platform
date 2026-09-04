package com.nit.review;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

	private final ReviewService reviewService;
	
	public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

	@PostMapping
	public Review createReview(@RequestBody Review review) {
		return reviewService.saveReview(review);
	}
	@GetMapping 
	public List<Review> getAllReviews(){
		return reviewService.getAllReviews();
	}
	
	@GetMapping("/{id}")
	public Review getReviewById(@PathVariable Long id) {
		return reviewService.getReviewById(id);
	}
	@DeleteMapping("/{id}")
	public String deleteReview(@PathVariable Long id) {
		reviewService.deleteReview(id);
		return "Review delete Sucessfully";
	}
	
	
	
}
