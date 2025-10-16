package com.microservices.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Async
    public void sendPasswordResetEmail(String toEmail, String username, String token) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request");
            message.setText(buildPasswordResetEmailBody(username, resetLink));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    private String buildPasswordResetEmailBody(String username, String resetLink) {
        return String.format(
                "Hello %s,\n\n" +
                        "You requested to reset your password. Click the link below to reset it:\n\n" +
                        "%s\n\n" +
                        "This link will expire in 60 minutes.\n\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "The Team",
                username,
                resetLink
        );
    }

    @Async
    public void sendAccountLockedEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Account Locked - Security Alert");
            message.setText(buildAccountLockedEmailBody(username));

            mailSender.send(message);
            log.info("Account locked email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send account locked email to: {}", toEmail, e);
        }
    }

    private String buildAccountLockedEmailBody(String username) {
        return String.format(
                "Hello %s,\n\n" +
                        "Your account has been locked due to multiple failed login attempts.\n\n" +
                        "If this was you, please contact support to unlock your account.\n" +
                        "If this was not you, your account may be under attack. Please contact support immediately.\n\n" +
                        "Best regards,\n" +
                        "The Team",
                username
        );
    }
}