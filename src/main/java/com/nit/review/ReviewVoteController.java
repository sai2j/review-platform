package com.nit.review;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review-votes")
public class ReviewVoteController {

    private final ReviewVoteService reviewVoteService;

    public ReviewVoteController(
            ReviewVoteService reviewVoteService) {

        this.reviewVoteService = reviewVoteService;
    }

    // =========================================
    // ADD / UPDATE VOTE
    // =========================================

    @PostMapping
    public ReviewVote saveVote(
            @RequestBody ReviewVote vote) {

        return reviewVoteService.saveVote(
                vote.getReviewId(),
                vote.getVoteType());
    }

    // =========================================
    // GET CURRENT USER VOTE
    // =========================================

    @GetMapping("/{reviewId}/user")
    public ReviewVote getUserVote(
            @PathVariable Long reviewId) {

        return reviewVoteService.getUserVote(
                reviewId);
    }

    // =========================================
    // GET VOTE COUNTS
    // =========================================

    @GetMapping("/{reviewId}/count")
    public Map<String, Long> getVoteCounts(
            @PathVariable Long reviewId) {

        long helpful =
                reviewVoteService
                        .getHelpfulCount(reviewId);

        long notHelpful =
                reviewVoteService
                        .getNotHelpfulCount(reviewId);

        Map<String, Long> response =
                new HashMap<>();

        response.put(
                "helpful",
                helpful);

        response.put(
                "notHelpful",
                notHelpful);

        return response;
    }

    // =========================================
    // DELETE CURRENT USER VOTE
    // =========================================

    @DeleteMapping("/{reviewId}/user")
    public String deleteVote(
            @PathVariable Long reviewId) {

        reviewVoteService.deleteVote(
                reviewId);

        return "Vote deleted successfully";
    }
}