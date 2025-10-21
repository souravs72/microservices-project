package com.microservices.userservice.service;

import com.microservices.userservice.dto.CreateUserRequest;
import com.microservices.userservice.dto.UpdateUserRequest;
import com.microservices.userservice.dto.UserDTO;
import com.microservices.userservice.entity.User;
import com.microservices.userservice.exception.ResourceNotFoundException;
import com.microservices.userservice.exception.UserAlreadyExistsException;
import com.microservices.userservice.repository.UserRepository;
import com.microservices.userservice.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        String sanitizedUsername = InputSanitizer.sanitizeUsername(request.getUsername());
        String sanitizedEmail = InputSanitizer.sanitizeEmail(request.getEmail());

        if (userRepository.existsByUsername(sanitizedUsername)) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(sanitizedEmail)) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(sanitizedUsername);
        user.setEmail(sanitizedEmail);
        user.setFirstName(request.getFirstName() != null ?
                InputSanitizer.sanitizeText(request.getFirstName()) : null);
        user.setLastName(request.getLastName() != null ?
                InputSanitizer.sanitizeText(request.getLastName()) : null);
        user.setPhone(request.getPhone() != null ?
                InputSanitizer.sanitizePhoneNumber(request.getPhone()) : null);
        user.setAddress(request.getAddress() != null ?
                InputSanitizer.sanitizeText(request.getAddress()) : null);
        user.setBio(request.getBio() != null ?
                InputSanitizer.sanitizeText(request.getBio()) : null);
        user.setProfilePictureUrl(request.getProfilePictureUrl() != null ?
                InputSanitizer.sanitizeText(request.getProfilePictureUrl()) : null);
        user.setRole(request.getRole() != null ? User.UserRole.valueOf(request.getRole()) : User.UserRole.USER);
        user.setMemberSince(LocalDateTime.now());
        user.setActive(true);

        User savedUser = userRepository.save(user);
        log.info("User created: {} from IP: {}", sanitizedUsername,
                com.microservices.userservice.security.SecurityContext.getContext().getIpAddress());
        return convertToDTO(savedUser);
    }

    @Transactional
    public UserDTO createUserFromSync(CreateUserRequest request) {
        String sanitizedUsername = InputSanitizer.sanitizeUsername(request.getUsername());
        String sanitizedEmail = InputSanitizer.sanitizeEmail(request.getEmail());

        // Check if user already exists (idempotency)
        if (userRepository.existsByUsername(sanitizedUsername)) {
            User existingUser = userRepository.findByUsername(sanitizedUsername)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            log.info("User already exists during sync: {}", sanitizedUsername);
            return convertToDTO(existingUser);
        }

        if (userRepository.existsByEmail(sanitizedEmail)) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(sanitizedUsername);
        user.setEmail(sanitizedEmail);
        user.setFirstName(request.getFirstName() != null ?
                InputSanitizer.sanitizeText(request.getFirstName()) : null);
        user.setLastName(request.getLastName() != null ?
                InputSanitizer.sanitizeText(request.getLastName()) : null);
        user.setPhone(request.getPhone() != null ?
                InputSanitizer.sanitizePhoneNumber(request.getPhone()) : null);
        user.setActive(true);

        User savedUser = userRepository.save(user);
        log.info("User synced from Auth Service: {}", sanitizedUsername);
        return convertToDTO(savedUser);
    }

    public UserDTO getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }

    public UserDTO getUserByUsername(String username) {
        String sanitizedUsername = InputSanitizer.sanitizeUsername(username);

        User user = userRepository.findByUsername(sanitizedUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username: " + sanitizedUsername));
        return convertToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean userExistsByUsername(String username) {
        String sanitizedUsername = InputSanitizer.sanitizeUsername(username);
        return userRepository.existsByUsername(sanitizedUsername);
    }

    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getEmail() != null) {
            String sanitizedEmail = InputSanitizer.sanitizeEmail(request.getEmail());
            if (!sanitizedEmail.equals(user.getEmail())) {
                if (userRepository.existsByEmail(sanitizedEmail)) {
                    throw new UserAlreadyExistsException("Email already exists");
                }
                user.setEmail(sanitizedEmail);
            }
        }

        if (request.getFirstName() != null) {
            user.setFirstName(InputSanitizer.sanitizeText(request.getFirstName()));
        }
        if (request.getLastName() != null) {
            user.setLastName(InputSanitizer.sanitizeText(request.getLastName()));
        }
        if (request.getPhone() != null) {
            user.setPhone(InputSanitizer.sanitizePhoneNumber(request.getPhone()));
        }
        if (request.getAddress() != null) {
            user.setAddress(InputSanitizer.sanitizeText(request.getAddress()));
        }
        if (request.getBio() != null) {
            user.setBio(InputSanitizer.sanitizeText(request.getBio()));
        }

        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        log.info("User updated: {} from IP: {}", user.getUsername(),
                com.microservices.userservice.security.SecurityContext.getContext().getIpAddress());
        return convertToDTO(updatedUser);
    }

    @Transactional
    public UserDTO toggleUserStatus(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setActive(!user.getActive());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        log.info("User status toggled: {} (now {}) from IP: {}", 
                user.getUsername(), 
                updatedUser.getActive() ? "active" : "inactive",
                com.microservices.userservice.security.SecurityContext.getContext().getIpAddress());
        
        return convertToDTO(updatedUser);
    }

    @Transactional
    public UserDTO uploadProfilePicture(Long id, MultipartFile file) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("File must be an image");
            }

            if (file.getSize() > 2 * 1024 * 1024) { // 2MB limit
                throw new IllegalArgumentException("File size must be less than 2MB");
            }

            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get("uploads/profile-pictures");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadDir.resolve(filename);

            // Save file
            Files.copy(file.getInputStream(), filePath);

            // Update user profile picture URL
            String profilePictureUrl = "/uploads/profile-pictures/" + filename;
            user.setProfilePictureUrl(profilePictureUrl);

            User updatedUser = userRepository.save(user);
            log.info("Profile picture uploaded for user: {} from IP: {}", 
                user.getUsername(), 
                com.microservices.userservice.security.SecurityContext.getContext().getIpAddress());

            return convertToDTO(updatedUser);

        } catch (IOException e) {
            log.error("Failed to upload profile picture for user: {}", user.getUsername(), e);
            throw new RuntimeException("Failed to upload profile picture", e);
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        log.warn("User deleted: ID {} from IP: {}", id,
                com.microservices.userservice.security.SecurityContext.getContext().getIpAddress());
    }

    public UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAddress(),
                user.getBio(),
                user.getProfilePictureUrl(),
                user.getRole(),
                user.getMemberSince(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getCreatedBy(),
                user.getUpdatedBy()
        );
    }
}