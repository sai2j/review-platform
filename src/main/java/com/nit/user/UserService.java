package com.nit.user;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nit.admin.AdminRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final AdminRepository adminRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, AdminRepository adminRepository,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.adminRepository = adminRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public User registerUser(String email, String password) {

		String normalizedEmail = email == null
				? ""
				: email.trim().toLowerCase();

		if (normalizedEmail.isBlank()) {
			throw new RuntimeException("Email is required");
		}

		if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
			throw new RuntimeException("Email already registered");
		}

		User user = new User();

		user.setEmail(normalizedEmail);
		user.setPassword(passwordEncoder.encode(password));
		user.setRole("USER");
		user.setStatus("ACTIVE");

		return userRepository.save(user);
	}

	public User saveUser(User user) {

		if (user.getEmail() != null) {
			user.setEmail(
					user.getEmail()
							.trim()
							.toLowerCase()
			);
		}

		if (user.getPassword() != null) {
			user.setPassword(
					passwordEncoder.encode(
							user.getPassword()
					)
			);
		}

		return userRepository.save(user);
	}

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public User getUserById(Long id) {
		return userRepository.findById(id).orElse(null);
	}

	public User getUserByEmail(String email) {

		String normalizedEmail = email == null
				? ""
				: email.trim().toLowerCase();

		return userRepository.findByEmailIgnoreCase(
				normalizedEmail
		);
	}

	public boolean isAdmin(Long userId) {
		return adminRepository.existsByUserId(userId);
	}

	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}

	public void deleteOwnAccount() {

		Authentication authentication =
				SecurityContextHolder
						.getContext()
						.getAuthentication();

		if (authentication == null
				|| !authentication.isAuthenticated()) {

			throw new AccessDeniedException(
					"You must be logged in"
			);
		}

		String email = authentication.getName();

		User user = userRepository.findByEmailIgnoreCase(email);

		if (user == null) {
			throw new RuntimeException("User not found");
		}

		Long userId = user.getId();

		user.setEmail(
				"deleted-user-"
						+ userId
						+ "@deleted.local"
		);

		user.setPassword(
				passwordEncoder.encode(
						UUID.randomUUID().toString()
				)
		);

		user.setStatus("RESTRICTED");

		userRepository.save(user);
	}

	public User updateUserStatus(Long id, String status) {

		User user =
				userRepository
						.findById(id)
						.orElse(null);

		if (user == null) {
			throw new RuntimeException("User not found");
		}

		if (status == null || status.isBlank()) {
			throw new RuntimeException("Status is required");
		}

		status = status.toUpperCase();

		if (!status.equals("ACTIVE")
				&& !status.equals("RESTRICTED")) {

			throw new RuntimeException(
					"Status must be ACTIVE or RESTRICTED"
			);
		}

		user.setStatus(status);

		return userRepository.save(user);
	}

	// ADMIN: Update an existing user's password
	public User updateUserPassword(
			Long id,
			String newPassword) {

		User user =
				userRepository
						.findById(id)
						.orElse(null);

		if (user == null) {
			throw new RuntimeException("User not found");
		}

		if (newPassword == null
				|| newPassword.isBlank()) {

			throw new RuntimeException(
					"Password is required"
			);
		}

		if (newPassword.length() < 8
				|| newPassword.length() > 100) {

			throw new RuntimeException(
					"Password must be between 8 and 100 characters"
			);
		}

		user.setPassword(
				passwordEncoder.encode(newPassword)
		);

		return userRepository.save(user);
	}
}