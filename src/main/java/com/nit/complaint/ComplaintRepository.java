package com.nit.complaint;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByReviewId(Long reviewId);

    List<Complaint> findByUserId(Long userId);

    List<Complaint> findByStatus(String status);
}