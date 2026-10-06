package com.nit.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ReviewRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldRejectNullRating() {

        ReviewRequestDTO dto =
                new ReviewRequestDTO(null, "Good website", 302L);

        Set<ConstraintViolation<ReviewRequestDTO>> violations =
                validator.validate(dto);

        assertEquals(1, violations.size());
    }

    @Test
    void shouldRejectRatingBelowOne() {

        ReviewRequestDTO dto =
                new ReviewRequestDTO(0, "Good website", 302L);

        Set<ConstraintViolation<ReviewRequestDTO>> violations =
                validator.validate(dto);

        assertEquals(1, violations.size());
    }

    @Test
    void shouldRejectRatingAboveFive() {

        ReviewRequestDTO dto =
                new ReviewRequestDTO(6, "Good website", 302L);

        Set<ConstraintViolation<ReviewRequestDTO>> violations =
                validator.validate(dto);

        assertEquals(1, violations.size());
    }

    @Test
    void shouldRejectBlankComment() {

        ReviewRequestDTO dto =
                new ReviewRequestDTO(5, "", 302L);

        Set<ConstraintViolation<ReviewRequestDTO>> violations =
                validator.validate(dto);

        assertEquals(1, violations.size());
    }

    @Test
    void shouldRejectNullWebsiteId() {

        ReviewRequestDTO dto =
                new ReviewRequestDTO(5, "Good website", null);

        Set<ConstraintViolation<ReviewRequestDTO>> violations =
                validator.validate(dto);

        assertEquals(1, violations.size());
    }

    @Test
    void shouldAcceptValidReviewRequest() {

        ReviewRequestDTO dto =
                new ReviewRequestDTO(5, "Good website", 302L);

        Set<ConstraintViolation<ReviewRequestDTO>> violations =
                validator.validate(dto);

        assertEquals(0, violations.size());
    }
}