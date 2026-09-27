package com.nit.user;

import java.util.HashMap;

import com.nit.business.BusinessclaimRepository;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import com.nit.security.BruteForceProtectionService;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;
	private final AuthenticationManager authenticationManager;
	private final BruteForceProtectionService bruteForceProtectionService;
	private final BusinessclaimRepository businessClaimRepository;

	public UserController(UserService userService, AuthenticationManager authenticationManager,
			BruteForceProtectionService bruteForceProtectionService,
			BusinessclaimRepository businessClaimRepository) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.bruteForceProtectionService = bruteForceProtectionService;
		this.businessClaimRepository = businessClaimRepository;
	}

	@PostMapping("/register")
	public User registerUser(@Valid @RequestBody User user) {
		return userService.registerUser(user.getEmail(), user.getPassword());
	}

	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> loginUser(@RequestBody User user, HttpServletRequest request,
			HttpServletResponse response) {

		String email = user.getEmail() == null ? "" : user.getEmail().trim().toLowerCase();
		String clientIp = request.getRemoteAddr();
		String protectionKey = clientIp + ":" + email;

		if (bruteForceProtectionService.isBlocked(protectionKey)) {

			Map<String, Object> errorResponse = new HashMap<>();

			errorResponse.put("error", "Too many failed login attempts. Please try again later.");

			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
		}

		try {

			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

			bruteForceProtectionService.recordSuccessfulLogin(protectionKey);

			SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

			securityContext.setAuthentication(authentication);

			SecurityContextHolder.setContext(securityContext);

			request.getSession(true);

			HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

			securityContextRepository.saveContext(securityContext, request, response);

			User loggedInUser = userService.getUserByEmail(user.getEmail());

			boolean admin = userService.isAdmin(loggedInUser.getId());

			Map<String, Object> result = new HashMap<>();

			result.put("user", loggedInUser);
			result.put("admin", admin);

			return ResponseEntity.ok(result);

		} catch (Exception e) {

			bruteForceProtectionService.recordFailedAttempt(protectionKey);

			Map<String, Object> errorResponse = new HashMap<>();

			errorResponse.put("error", "Invalid email or password");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		}
	}

	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/me")
	public ResponseEntity<Map<String, String>> deleteMyAccount(HttpServletRequest request,
			HttpServletResponse response) {

		userService.deleteOwnAccount();

		SecurityContextHolder.clearContext();

		var session = request.getSession(false);

		if (session != null) {
			session.invalidate();
		}

		Map<String, String> result = new HashMap<>();

		result.put("message", "Your account has been deleted.");

		return ResponseEntity.ok(result);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	public User createUser(@Valid @RequestBody User user) {
		return userService.saveUser(user);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping
	public List<User> getAllUsers() {
		return userService.getAllUsers();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{id}")
	public User getUserById(@PathVariable Long id) {
		return userService.getUserById(id);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{id}/status")
	public User updateUserStatus(@PathVariable Long id, @RequestParam String status) {
		return userService.updateUserStatus(id, status);
	}

	// ADMIN: Update an existing user's password
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{id}/password")
	public User updateUserPassword(@PathVariable Long id,
			@RequestParam String newPassword) {

		return userService.updateUserPassword(id, newPassword);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public String deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return "User deleted successfully";
	}
}