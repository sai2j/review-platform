package com.nit.review;

import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nit.user.User;
import com.nit.user.UserRepository;

@Service
public class ReviewVoteService {

	private final ReviewVoteRepository reviewVoteRepository;
	private final UserRepository userRepository;
	private final ReviewRepository reviewRepository;

	public ReviewVoteService(ReviewVoteRepository reviewVoteRepository, UserRepository userRepository,
			ReviewRepository reviewRepository) {
		this.reviewVoteRepository = reviewVoteRepository;
		this.userRepository = userRepository;
		this.reviewRepository = reviewRepository;
	}

	public ReviewVote saveVote(Long reviewId, String voteType) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String loggedInEmail = authentication.getName();
		User loggedInUser = userRepository.findByEmail(loggedInEmail);
		if (loggedInUser == null) {
			throw new RuntimeException("User not found");
		}
		if (!reviewRepository.existsById(reviewId)) {
			throw new RuntimeException("Review does not exist");
		}
		if (!"HELPFUL".equals(voteType) && !"NOT_HELPFUL".equals(voteType)) {
			throw new RuntimeException("Invalid vote type");
		}
		Long userId = loggedInUser.getId();
		Optional<ReviewVote> existingVote = reviewVoteRepository.findByReviewIdAndUserId(reviewId, userId);
		if (existingVote.isPresent()) {
			ReviewVote vote = existingVote.get();
			vote.setVoteType(voteType);
			return reviewVoteRepository.save(vote);
		}
		ReviewVote newVote = new ReviewVote(reviewId, userId, voteType);
		return reviewVoteRepository.save(newVote);
	}
	public ReviewVote getUserVote(Long reviewId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String loggedInEmail = authentication.getName();
		User loggedInUser = userRepository.findByEmail(loggedInEmail);
		if (loggedInUser == null) {
			throw new RuntimeException("User not found");
		}
		return reviewVoteRepository.findByReviewIdAndUserId(reviewId, loggedInUser.getId()).orElse(null);
	}
	public long getHelpfulCount(Long reviewId) {
		return reviewVoteRepository.countByReviewIdAndVoteType(reviewId, "HELPFUL");
	}
	public long getNotHelpfulCount(Long reviewId) {
		return reviewVoteRepository.countByReviewIdAndVoteType(reviewId, "NOT_HELPFUL");
	}
	public void deleteVote(Long reviewId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String loggedInEmail = authentication.getName();
		User loggedInUser = userRepository.findByEmail(loggedInEmail);
		if (loggedInUser == null) {
			throw new RuntimeException("User not found");
		}
		Optional<ReviewVote> existingVote = reviewVoteRepository.findByReviewIdAndUserId(reviewId,
				loggedInUser.getId());
		if (existingVote.isPresent()) {
			reviewVoteRepository.delete(existingVote.get());
		}
	}
}