package com.nit.Website;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebsiteRepository extends JpaRepository<Website, Long> {

    Optional<Website> findByCanonicalDomain(String canonicalDomain);
    @Query("SELECT w FROM Website w WHERE w.url = :url")

    Optional<Website> findByUrl(String url);

    List<Website> findByNameContainingIgnoreCaseOrCanonicalDomainContainingIgnoreCase(
            String name,
            String canonicalDomain);
}