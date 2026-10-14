
package com.nit.user;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${app.base-url:http://localhost:8080/review-platform}")
    private String baseUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            JavaMailSender mailSender,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void requestPasswordReset(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        User user = userRepository.findByEmailIgnoreCase(
                email.trim().toLowerCase()
        );

        // Do not reveal whether the account exists.
        if (user == null) {
            return;
        }

        tokenRepository.deleteByUserId(user.getId());

        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(user.getId());
        resetToken.setToken(token);
        resetToken.setExpiryDate(
                LocalDateTime.now().plusMinutes(30)
        );
        resetToken.setUsed(0);

        tokenRepository.save(resetToken);

        String resetLink = baseUrl
                + "/reset-password.html?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reset Your Review Platform Password");
        message.setText(
                "Hello,\n\n"
                + "A password reset was requested for your account.\n"
                + "Open this link within 30 minutes to reset your password:\n\n"
                + resetLink + "\n\n"
                + "If you did not request this, ignore this email."
        );

        mailSender.send(message);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Reset token is required");
        }

        if (newPassword == null
                || newPassword.length() < 8
                || newPassword.length() > 100) {
            throw new IllegalArgumentException(
                    "Password must be between 8 and 100 characters"
            );
        }

        PasswordResetToken resetToken = tokenRepository
                .findByToken(token.trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid or expired reset token"
                        )
                );

        if (resetToken.getUsed() == null
                || resetToken.getUsed() != 0
                || resetToken.getExpiryDate() == null
                || !resetToken.getExpiryDate().isAfter(
                        LocalDateTime.now()
                )) {
            throw new IllegalArgumentException(
                    "Invalid or expired reset token"
            );
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(1);
        tokenRepository.save(resetToken);

        // Remove the used token so it cannot be reused.
        tokenRepository.deleteByToken(token.trim());
    }
}
