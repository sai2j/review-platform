package com.nit.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nit.user.User;
import com.nit.user.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email)
			throws UsernameNotFoundException {

		String normalizedEmail = email == null
				? ""
				: email.trim().toLowerCase();

		User user =
				userRepository.findByEmailIgnoreCase(
						normalizedEmail
				);

		if (user == null) {

			throw new UsernameNotFoundException(
					"User not found with email: "
							+ normalizedEmail
			);
		}

		if ("RESTRICTED".equalsIgnoreCase(
				user.getStatus())) {

			throw new UsernameNotFoundException(
					"User account is restricted"
			);
		}

		String role =
				user.getRole() == null
						|| user.getRole().isBlank()
						? "USER"
						: user.getRole().toUpperCase();

		return org.springframework.security.core.userdetails.User
				.withUsername(user.getEmail())
				.password(user.getPassword())
				.roles(role)
				.build();
	}
}