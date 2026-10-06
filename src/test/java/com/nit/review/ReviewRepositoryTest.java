package com.nit.review;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.test.database.replace=NONE"
})
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    // ==========================================
    // CUSTOM QUERY TEST - AVERAGE RATING
    // PENDING + APPROVED
    // ==========================================

    @Test
    void shouldFindAverageCountedRating() {

        Long websiteId = 302L;

        Double result =
                reviewRepository.findAverageCountedRating(
                        websiteId
                );

        if (result != null) {

            assertEquals(
                    result,
                    reviewRepository.findAverageCountedRating(
                            websiteId
                    ),
                    0.0
            );
        }
    }

    // ==========================================
    // CUSTOM QUERY TEST - COUNT
    // PENDING + APPROVED
    // ==========================================

    @Test
    void shouldCountCountedReviews() {

        Long websiteId = 302L;

        Long result =
                reviewRepository.countCountedReviews(
                        websiteId
                );

        assertEquals(
                result,
                reviewRepository.countCountedReviews(
                        websiteId
                )
        );
    }

    // ==========================================
    // CUSTOM QUERY TEST - COUNT BY RATING
    // PENDING + APPROVED
    // ==========================================

    @Test
    void shouldCountCountedReviewsByRating() {

        Long websiteId = 302L;

        Integer rating = 5;

        Long result =
                reviewRepository.countCountedReviewsByRating(
                        websiteId,
                        rating
                );

        assertEquals(
                result,
                reviewRepository.countCountedReviewsByRating(
                        websiteId,
                        rating
                )
        );
    }
}