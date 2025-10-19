package com.microservices.auth.config;

import com.microservices.auth.entity.User;
import com.microservices.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.admin.enabled:true}")
    private boolean adminEnabled;

    @Override
    public void run(String... args) throws Exception {
        initializeAdminUser();
        log.info("Data initialization completed");
    }

    private void initializeAdminUser() {
        // Check if admin user already exists
        if (userRepository.existsByUsername(adminUsername)) {
            log.info("Admin user '{}' already exists, skipping initialization", adminUsername);
            return;
        }

        // Create admin user
        User adminUser = new User();
        adminUser.setUsername(adminUsername);
        adminUser.setEmail(adminEmail);
        adminUser.setPassword(passwordEncoder.encode(adminPassword));
        adminUser.setRole("ADMIN");
        adminUser.setEnabled(adminEnabled);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        adminUser.setFailedLoginAttempts(0);
        adminUser.setAccountLocked(false);

        try {
            User savedAdmin = userRepository.save(adminUser);
            log.info("✅ Admin user created successfully:");
            log.info("   Username: {}", savedAdmin.getUsername());
            log.info("   Email: {}", savedAdmin.getEmail());
            log.info("   Role: {}", savedAdmin.getRole());
            log.info("   Enabled: {}", savedAdmin.getEnabled());
            log.info("   ID: {}", savedAdmin.getId());
            
            if (!adminEnabled) {
                log.warn("⚠️  Admin user is disabled. Enable it by setting app.admin.enabled=true");
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to create admin user: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize admin user", e);
        }
    }
}
