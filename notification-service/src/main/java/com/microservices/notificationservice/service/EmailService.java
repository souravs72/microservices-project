package com.microservices.notificationservice.service;

import com.microservices.notificationservice.dto.UserCreatedEvent;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final Counter emailSentCounter;
    private final Counter emailFailedCounter;
    private final Timer emailSendingTimer;
    private final Configuration freemarkerConfig;

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender,
                       @Qualifier("emailSentCounter") Counter emailSentCounter,
                       @Qualifier("emailFailedCounter") Counter emailFailedCounter,
                       @Qualifier("emailSendingTimer") Timer emailSendingTimer,
                       Configuration freemarkerConfig) {
        this.mailSender = mailSender;
        this.emailSentCounter = emailSentCounter;
        this.emailFailedCounter = emailFailedCounter;
        this.emailSendingTimer = emailSendingTimer;
        this.freemarkerConfig = freemarkerConfig;
    }

    @Async
    @CircuitBreaker(name = "emailService", fallbackMethod = "sendWelcomeEmailFallback")
    @Retry(name = "emailService")
    @Bulkhead(name = "emailService")
    @RateLimiter(name = "emailService")
    public CompletableFuture<Void> sendWelcomeEmail(UserCreatedEvent event) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start();
            try {
                String recipientEmail = event.getEmail();
                String recipientName = buildFullName(event);
                String subject = "🎉 Welcome to Our Platform!";
                
                // Prepare template data
                Map<String, Object> templateData = new HashMap<>();
                templateData.put("username", recipientName);
                templateData.put("email", recipientEmail);
                templateData.put("firstName", event.getFirstName());
                templateData.put("lastName", event.getLastName());

                // Create HTML email using template
                MimeMessagePreparator messagePreparator = mimeMessage -> {
                    MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                    messageHelper.setFrom(fromEmail);
                    messageHelper.setTo(recipientEmail);
                    messageHelper.setSubject(subject);
                    
                    try {
                        Template template = freemarkerConfig.getTemplate("emails/welcome-email.html");
                        String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, templateData);
                        messageHelper.setText(htmlContent, true);
                    } catch (Exception e) {
                        log.warn("Failed to load HTML template, falling back to plain text", e);
                        String plainTextBody = buildWelcomeEmailBody(event);
                        messageHelper.setText(plainTextBody, false);
                    }
                };

                // Send email
                mailSender.send(messagePreparator);
                emailSentCounter.increment();
                log.info("Welcome email sent successfully to: {}", recipientEmail);
                
            } catch (Exception e) {
                emailFailedCounter.increment();
                log.error("Failed to send welcome email to: {}", event.getEmail(), e);
                throw new RuntimeException("Failed to send email", e);
            } finally {
                sample.stop(emailSendingTimer);
            }
        });
    }

    public CompletableFuture<Void> sendWelcomeEmailFallback(UserCreatedEvent event, Exception ex) {
        log.error("Email service fallback triggered for user: {} due to: {}", 
                event.getEmail(), ex.getMessage());
        
        // In fallback, we could:
        // 1. Store in a retry queue
        // 2. Send to a different email service
        // 3. Log for manual intervention
        
        emailFailedCounter.increment();
        return CompletableFuture.completedFuture(null);
    }


    private String buildFullName(UserCreatedEvent event) {
        StringBuilder name = new StringBuilder();
        if (event.getFirstName() != null && !event.getFirstName().isEmpty()) {
            name.append(event.getFirstName());
        }
        if (event.getLastName() != null && !event.getLastName().isEmpty()) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(event.getLastName());
        }
        if (name.length() == 0) {
            return event.getUsername();
        }
        return name.toString();
    }

    private String buildWelcomeEmailBody(UserCreatedEvent event) {
        String name = buildFullName(event);
        return String.format(
                "Hello %s,\n\n" +
                        "Welcome to our platform! Your account has been successfully created.\n" +
                        "Username: %s\n\n" +
                        "You can now access all features of our service.\n\n" +
                        "Best regards,\n" +
                        "The Team",
                name,
                event.getUsername()
        );
    }
}