package com.nit.user;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nit.admin.AdminRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String email, String password) {

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();

        user.setEmail(email);

        // Password hash
        user.setPassword(passwordEncoder.encode(password));

        user.setRole("USER");

        return userRepository.save(user);
    }

    public User saveUser(User user) {

        if (user.getPassword() != null) {
            user.setPassword(
                    passwordEncoder.encode(user.getPassword())
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
        return userRepository.findByEmail(email);
    }

    public boolean isAdmin(Long userId) {
        return adminRepository.existsByUserId(userId);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}