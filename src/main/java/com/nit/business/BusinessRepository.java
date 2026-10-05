package com.nit.business;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BusinessRepository extends JpaRepository<Business, Long> {

	@Query("""
			SELECT b
			FROM Business b
			WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
			   OR LOWER(b.officialUrl) LIKE LOWER(CONCAT('%', :keyword, '%'))
			""")
	List<Business> searchBusinesses(@Param("keyword") String keyword);

}