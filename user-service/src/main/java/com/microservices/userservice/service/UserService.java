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

        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        log.info("User updated: {} from IP: {}", user.getUsername(),
                com.microservices.userservice.security.SecurityContext.getContext().getIpAddress());
        return convertToDTO(updatedUser);
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

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}