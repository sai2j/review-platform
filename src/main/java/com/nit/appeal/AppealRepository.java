package com.nit.appeal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppealRepository extends JpaRepository<Appeal, Long> {

    List<Appeal> findByReviewId(Long reviewId);

    List<Appeal> findByUserId(Long userId);

    List<Appeal> findByStatus(String status);
}