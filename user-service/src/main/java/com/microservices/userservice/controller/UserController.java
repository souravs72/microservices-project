package com.microservices.userservice.controller;

import com.microservices.userservice.dto.CreateUserRequest;
import com.microservices.userservice.dto.UpdateUserRequest;
import com.microservices.userservice.dto.UserDTO;
import com.microservices.userservice.security.AdminOnly;
import com.microservices.userservice.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.debug("REST API: Creating user with username {}", request.getUsername());
        UserDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getUserById(
            @PathVariable
            @Min(value = 1, message = "ID must be positive")
            Long id) {
        log.debug("REST API: Getting user by id {}", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getUserByUsername(
            @PathVariable
            @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$",
                    message = "Username must be 3-50 alphanumeric characters or underscores")
            String username) {
        log.debug("REST API: Getting user by username {}", username);
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.debug("REST API: Getting all users");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable
            @Min(value = 1, message = "ID must be positive")
            Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.debug("REST API: Updating user with id {}", id);
        UserDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> toggleUserStatus(
            @PathVariable
            @Min(value = 1, message = "ID must be positive")
            Long id) {
        log.debug("REST API: Toggling user status for id {}", id);
        UserDTO user = userService.toggleUserStatus(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{id}/profile-picture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> uploadProfilePicture(
            @PathVariable
            @Min(value = 1, message = "ID must be positive")
            Long id,
            @RequestParam("file") MultipartFile file) {
        log.debug("REST API: Uploading profile picture for user id {}", id);
        UserDTO user = userService.uploadProfilePicture(id, file);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Void> deleteUser(
            @PathVariable
            @Min(value = 1, message = "ID must be positive")
            Long id) {
        log.debug("REST API: Deleting user with id {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Internal endpoint for Auth Service to sync user creation
     * Protected by X-Internal-API-Key header
     */
    @PostMapping("/sync")
    public ResponseEntity<UserDTO> syncUser(@RequestBody CreateUserRequest request) {
        try {
            log.info("Sync endpoint called for user: {}", request.getUsername());
            UserDTO user = userService.createUserFromSync(request);
            return ResponseEntity.status(
                    user.getCreatedAt().equals(user.getUpdatedAt()) ?
                            HttpStatus.CREATED : HttpStatus.OK
            ).body(user);
        } catch (Exception e) {
            log.error("Failed to sync user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}