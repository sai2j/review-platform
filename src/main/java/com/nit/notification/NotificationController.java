package com.nit.notification;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nit.user.User;
import com.nit.user.UserService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(
            NotificationService notificationService,
            UserService userService) {

        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                notificationService.getUserNotifications(userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                notificationService.markAsRead(id, userId));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            Authentication authentication) {

        Long userId = getUserId(authentication);

        notificationService.markAllAsRead(userId);

        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new RuntimeException(
                    "User is not authenticated.");
        }

        User user = userService.getUserByEmail(
                authentication.getName());

        if (user == null) {
            throw new RuntimeException(
                    "Authenticated user not found.");
        }

        return user.getId();
    }
}