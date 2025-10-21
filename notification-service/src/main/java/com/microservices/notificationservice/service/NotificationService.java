package com.microservices.notificationservice.service;

import com.microservices.notificationservice.dto.UserCreatedEvent;
import com.microservices.notificationservice.entity.NotificationHistory;
import com.microservices.notificationservice.repository.NotificationHistoryRepository;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationHistoryRepository historyRepository;
    private final Configuration freemarkerConfig;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Transactional
    public CompletableFuture<Void> processUserCreatedEvent(UserCreatedEvent userEvent, String eventId) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Processing user created event for user: {}", userEvent.getUsername());

                // Check if we've already processed this event
                if (historyRepository.existsByEventId(eventId)) {
                    log.warn("Event {} already processed, skipping", eventId);
                    return;
                }

                // Create notification history record
                NotificationHistory notification = NotificationHistory.builder()
                    .eventId(eventId)
                    .eventType(userEvent.getEventType())
                    .recipientEmail(userEvent.getEmail())
                    .recipientName(userEvent.getFirstName() + " " + userEvent.getLastName())
                    .subject("Welcome to Microservices Platform!")
                    .status(NotificationHistory.NotificationStatus.PENDING)
                    .retryCount(0)
                    .maxRetries(3)
                    .build();

                historyRepository.save(notification);

                // Send welcome email
                sendWelcomeEmail(userEvent, notification);

            } catch (Exception e) {
                log.error("Error processing user created event: {}", eventId, e);
                throw new RuntimeException("Failed to process user created event", e);
            }
        });
    }

    private void sendWelcomeEmail(UserCreatedEvent userEvent, NotificationHistory notification) {
        try {
            log.info("Sending welcome email to: {}", userEvent.getEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(userEvent.getEmail());
            helper.setSubject("Welcome to Microservices Platform!");

            // Prepare template data
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("firstName", userEvent.getFirstName());
            templateData.put("lastName", userEvent.getLastName());
            templateData.put("username", userEvent.getUsername());
            templateData.put("email", userEvent.getEmail());
            templateData.put("currentYear", LocalDateTime.now().getYear());

            // Generate HTML content using FreeMarker template
            String htmlContent = generateEmailContent("welcome-email.html", templateData);
            helper.setText(htmlContent, true);

            // Send email
            mailSender.send(message);

            // Update notification status
            notification.setStatus(NotificationHistory.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setContent(htmlContent);
            historyRepository.save(notification);

            log.info("Welcome email sent successfully to: {}", userEvent.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", userEvent.getEmail(), e);
            handleEmailFailure(notification, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending welcome email to: {}", userEvent.getEmail(), e);
            handleEmailFailure(notification, e.getMessage());
        }
    }

    private String generateEmailContent(String templateName, Map<String, Object> data) {
        try {
            Template template = freemarkerConfig.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(data, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error generating email content from template: {}", templateName, e);
            // Fallback to simple HTML
            return generateFallbackEmailContent(data);
        }
    }

    private String generateFallbackEmailContent(Map<String, Object> data) {
        return String.format("""
            <html>
            <body>
                <h2>Welcome to Microservices Platform!</h2>
                <p>Hello %s %s,</p>
                <p>Welcome to our microservices platform! Your account has been successfully created.</p>
                <p>Username: %s</p>
                <p>Email: %s</p>
                <p>Best regards,<br>The Microservices Team</p>
            </body>
            </html>
            """, 
            data.get("firstName"), 
            data.get("lastName"), 
            data.get("username"), 
            data.get("email"));
    }

    private void handleEmailFailure(NotificationHistory notification, String errorMessage) {
        notification.setStatus(NotificationHistory.NotificationStatus.FAILED);
        notification.setErrorMessage(errorMessage);
        notification.setRetryCount(notification.getRetryCount() + 1);
        historyRepository.save(notification);
    }
}
