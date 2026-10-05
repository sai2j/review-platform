package com.nit.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmail(String email);

	boolean existsByEmailIgnoreCase(String email);

	User findByEmail(String email);

	User findByEmailIgnoreCase(String email);
}