package com.nit.Report;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository
        extends JpaRepository<Report, Long> {

    // Delete all reports linked to a review
    void deleteByReviewId(Long reviewId);
}