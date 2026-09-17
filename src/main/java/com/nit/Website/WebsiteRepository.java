package com.nit.Website;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WebsiteRepository extends JpaRepository<Website, Long> {

Optional<Website> findByCanonicalDomain(String canonicalDomain);

@Query("SELECT w FROM Website w WHERE w.url = :url")
Optional<Website> findByUrl(@Param("url") String url);

List<Website> findByNameContainingIgnoreCaseOrCanonicalDomainContainingIgnoreCase(
        String name,
        String canonicalDomain);

// RELATED WEBSITES
@Query("""
        SELECT w FROM Website w
        WHERE w.id <> :websiteId
        AND (
            LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        """)
List<Website> findRelatedWebsites(
        @Param("websiteId") Long websiteId,
        @Param("keyword") String keyword);


}
