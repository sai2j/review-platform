package com.nit.review;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {

    Optional<ReviewVote> findByReviewIdAndUserId(
            Long reviewId,
            Long userId);

    long countByReviewIdAndVoteType(
            Long reviewId,
            String voteType);

    void deleteByReviewId(Long reviewId);
}