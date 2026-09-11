package com.nit.review;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nit.Website.WebsiteRepository;
import com.nit.user.User;
import com.nit.user.UserRepository;

@Service
public class ReviewService {

    public final ReviewRepository reviewRepository;

    private final UserRepository userRepository;

    private final WebsiteRepository websiteRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            WebsiteRepository websiteRepository) {

        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.websiteRepository = websiteRepository;
    }

    public Review saveReview(Review review) {

        // Get currently logged-in user
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String loggedInEmail = authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {
            throw new RuntimeException("User not found");
        }

        // IMPORTANT:
        // Do not trust userId coming from frontend.
        // Always use logged-in user's actual ID.
        review.setUserId(loggedInUser.getId());

        // Check website
        if (!websiteRepository.existsById(review.getWebsiteId())) {
            throw new RuntimeException("Website does not exist");
        }

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

        Review existingReview =
                reviewRepository.findById(id).orElse(null);

        if (existingReview == null) {
            return null;
        }

        // Get currently logged-in user
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String loggedInEmail = authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {
            throw new RuntimeException("User not found");
        }

        // Check review ownership
        if (!existingReview.getUserId().equals(loggedInUser.getId())) {
            throw new AccessDeniedException(
                    "You can edit only your own review");
        }

        // Update only rating and comment
        existingReview.setRating(review.getRating());
        existingReview.setComment(review.getComment());

        return reviewRepository.save(existingReview);
    }

    public void deleteReview(Long id) {

        Review existingReview =
                reviewRepository.findById(id).orElse(null);

        if (existingReview == null) {
            throw new RuntimeException("Review not found");
        }

        // Get currently logged-in user
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String loggedInEmail = authentication.getName();

        User loggedInUser =
                userRepository.findByEmail(loggedInEmail);

        if (loggedInUser == null) {
            throw new RuntimeException("User not found");
        }

        // Check review ownership
        if (!existingReview.getUserId().equals(loggedInUser.getId())) {
            throw new AccessDeniedException(
                    "You can delete only your own review");
        }

        reviewRepository.deleteById(id);
    }

    public double getAverageRating(Long websiteId) {

        List<Review> reviews =
                reviewRepository.findByWebsiteId(websiteId);

        if (reviews.isEmpty()) {
            return 0.0;
        }

        double totalRating = 0;

        for (Review review : reviews) {
            totalRating += review.getRating();
        }

        return Math.round(
                (totalRating / reviews.size()) * 10.0
        ) / 10.0;
    }

    public int getReviewCount(Long websiteId) {

        List<Review> reviews =
                reviewRepository.findByWebsiteId(websiteId);

        return reviews.size();
    }

    public int getFiveStarCount(Long websiteId) {
        return getStarCount(websiteId, 5);
    }

    public int getFourStarCount(Long websiteId) {
        return getStarCount(websiteId, 4);
    }

    public int getThreeStarCount(Long websiteId) {
        return getStarCount(websiteId, 3);
    }

    public int getTwoStarCount(Long websiteId) {
        return getStarCount(websiteId, 2);
    }

    public int getOneStarCount(Long websiteId) {
        return getStarCount(websiteId, 1);
    }

    private int getStarCount(Long websiteId, int star) {

        List<Review> reviews =
                reviewRepository.findByWebsiteId(websiteId);

        int count = 0;

        for (Review review : reviews) {

            if (review.getRating() != null
                    && review.getRating() == star) {

                count++;
            }
        }

        return count;
    }
}