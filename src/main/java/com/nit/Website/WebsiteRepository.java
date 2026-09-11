package com.nit.Website;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WebsiteRepository extends JpaRepository<Website, Long> {

    Optional<Website> findByCanonicalDomain(String canonicalDomain);

    Optional<Website> findByUrl(String url);
}