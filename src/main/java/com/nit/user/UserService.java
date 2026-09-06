package com.nit.user;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nit.admin.AdminRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    public UserService(UserRepository userRepository,AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }
    public User registerUser(String email,String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        return userRepository.save(user);
    }
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    public User loginUser(String email,String password) {
        return userRepository.findByEmailAndPassword(email,password);
    }
    public boolean isAdmin(Long userId) {
        return adminRepository.existsByUserId(userId);
    }
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}