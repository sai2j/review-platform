package com.nit.business;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class BusinessEmailVerificationService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080/review-platform}")
    private String baseUrl;

    public BusinessEmailVerificationService(
            JavaMailSender mailSender) {

        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(
            Business business) {

        if (business.getBusinessEmail() == null
                || business.getBusinessEmail().isBlank()) {

            throw new RuntimeException(
                    "Business email is required");
        }

        String verificationLink =
                baseUrl
                + "/businesses/"
                + business.getId()
                + "/verify-email?token="
                + business.getEmailVerificationToken();

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(
                business.getBusinessEmail());

        message.setSubject(
                "Business Email Verification - Review Platform");

        message.setText(
                "Hello " + business.getName() + ",\n\n"
                + "Please verify your business email by opening "
                + "the following link:\n\n"
                + verificationLink
                + "\n\n"
                + "This verification link is valid for 24 hours.\n\n"
                + "Review Platform");

        mailSender.send(message);
    }
}