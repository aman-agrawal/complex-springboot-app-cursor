package com.example.complexapp.service;

import com.example.complexapp.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendWelcomeEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Welcome to Complex Spring Boot App!");
            message.setText(String.format(
                "Hello %s,\n\nWelcome to our application! Your account has been created successfully.\n\n" +
                "Username: %s\nEmail: %s\n\n" +
                "Please verify your email address to activate your account.\n\n" +
                "Best regards,\nThe Team",
                user.getFirstName(),
                user.getUsername(),
                user.getEmail()
            ));
            
            mailSender.send(message);
            log.info("Welcome email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendPasswordResetEmail(User user, String tempPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Password Reset Request");
            message.setText(String.format(
                "Hello %s,\n\nYou have requested a password reset for your account.\n\n" +
                "Your temporary password is: %s\n\n" +
                "Please change your password after logging in.\n\n" +
                "If you did not request this reset, please ignore this email.\n\n" +
                "Best regards,\nThe Team",
                user.getFirstName(),
                tempPassword
            ));
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendEmailVerification(User user, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Email Verification");
            message.setText(String.format(
                "Hello %s,\n\nPlease verify your email address by clicking the link below:\n\n" +
                "http://localhost:8080/api/auth/verify-email?token=%s\n\n" +
                "If you did not create an account, please ignore this email.\n\n" +
                "Best regards,\nThe Team",
                user.getFirstName(),
                verificationToken
            ));
            
            mailSender.send(message);
            log.info("Email verification sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email verification to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendAccountLockedNotification(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Account Locked");
            message.setText(String.format(
                "Hello %s,\n\nYour account has been locked due to multiple failed login attempts.\n\n" +
                "Please contact support to unlock your account.\n\n" +
                "Best regards,\nThe Team",
                user.getFirstName()
            ));
            
            mailSender.send(message);
            log.info("Account locked notification sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send account locked notification to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendOrderConfirmation(User user, String orderNumber) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Order Confirmation - " + orderNumber);
            message.setText(String.format(
                "Hello %s,\n\nThank you for your order! Your order has been confirmed.\n\n" +
                "Order Number: %s\n\n" +
                "We will send you updates on your order status.\n\n" +
                "Best regards,\nThe Team",
                user.getFirstName(),
                orderNumber
            ));
            
            mailSender.send(message);
            log.info("Order confirmation sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send order confirmation to: {}", user.getEmail(), e);
        }
    }
}
