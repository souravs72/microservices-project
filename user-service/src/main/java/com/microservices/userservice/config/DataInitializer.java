package com.microservices.userservice.config;

import com.microservices.userservice.entity.User;
import com.microservices.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.admin.firstName:Admin}")
    private String adminFirstName;

    @Value("${app.admin.lastName:User}")
    private String adminLastName;

    @Value("${app.admin.enabled:true}")
    private boolean adminEnabled;

    @Override
    public void run(String... args) throws Exception {
        initializeAdminUser();
        log.info("User service data initialization completed");
    }

    private void initializeAdminUser() {
        try {
            // Check if admin user already exists by username or email
            if (userRepository.existsByUsername(adminUsername) || userRepository.existsByEmail(adminEmail)) {
                log.info("Admin user profile '{}' or email '{}' already exists in user service, skipping initialization", adminUsername, adminEmail);
                return;
            }

            // Create admin user profile
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setEmail(adminEmail);
            adminUser.setFirstName(adminFirstName);
            adminUser.setLastName(adminLastName);
            adminUser.setActive(adminEnabled);
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser.setUpdatedAt(LocalDateTime.now());
            adminUser.setRole(User.UserRole.ADMIN);
            adminUser.setMemberSince(LocalDateTime.now());

            User savedAdmin = userRepository.save(adminUser);
            log.info("✅ Admin user profile created successfully in user service:");
            log.info("   Username: {}", savedAdmin.getUsername());
            log.info("   Email: {}", savedAdmin.getEmail());
            log.info("   Name: {} {}", savedAdmin.getFirstName(), savedAdmin.getLastName());
            log.info("   Active: {}", savedAdmin.getActive());
            log.info("   ID: {}", savedAdmin.getId());
            
            if (!adminEnabled) {
                log.warn("⚠️  Admin user profile is inactive. Enable it by setting app.admin.enabled=true");
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to create admin user profile: {}", e.getMessage());
            // Don't throw exception, just log the error and continue
            log.warn("Continuing without admin user initialization");
        }
    }
}
