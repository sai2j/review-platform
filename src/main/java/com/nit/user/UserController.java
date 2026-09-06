package com.nit.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user.getEmail(),user.getPassword());
    }
    @PostMapping("/login")
    public Map<String, Object> loginUser(@RequestBody User user) {
        User loggedInUser =userService.loginUser(user.getEmail(),user.getPassword());
        if (loggedInUser == null) {
            return null;
        }
        boolean admin =userService.isAdmin(loggedInUser.getId());
        Map<String, Object> response =new HashMap<>();
        response.put("user",loggedInUser);
        response.put("admin",admin);
        return response;
    }
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "User deleted successfully";
    }
}