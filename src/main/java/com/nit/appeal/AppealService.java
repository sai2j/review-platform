package com.nit.appeal;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nit.review.ReviewRepository;
import com.nit.user.User;
import com.nit.user.UserRepository;

@Service
public class AppealService {

    private final AppealRepository appealRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public AppealService(
            AppealRepository appealRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository) {

        this.appealRepository = appealRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    public Appeal createAppeal(
            Long reviewId,
            String reason) {

        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Review not found");
        }

        User loggedInUser = getLoggedInUser();

        Appeal appeal = new Appeal(
                reviewId,
                loggedInUser.getId(),
                reason
        );

        return appealRepository.save(appeal);
    }

    public List<Appeal> getAppealsByReviewId(Long reviewId) {

        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Review not found");
        }

        return appealRepository.findByReviewId(reviewId);
    }

    public List<Appeal> getAllAppeals() {
        return appealRepository.findAll();
    }

    public Appeal getAppealById(Long id) {

        return appealRepository
                .findById(id)
                .orElse(null);
    }

    public Appeal updateAppealStatus(
            Long id,
            String status,
            String decision) {

        Appeal appeal = appealRepository
                .findById(id)
                .orElse(null);

        if (appeal == null) {
            return null;
        }

        appeal.setStatus(status);
        appeal.setDecision(decision);

        return appealRepository.save(appeal);
    }

    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "You must be logged in");
        }

        User user =
                userRepository.findByEmail(
                        authentication.getName());

        if (user == null) {
            throw new AccessDeniedException(
                    "User not found");
        }

        return user;
    }
}