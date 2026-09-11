package com.nit.business;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessclaimRepository extends JpaRepository<BusinessClaim, Long> {

    Optional<BusinessClaim> findByUserIdAndStatus(Long userId, String status);

}