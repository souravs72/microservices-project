package com.microservices.userservice.controller;

import com.microservices.userservice.dto.CreateUserRequest;
import com.microservices.userservice.dto.UpdateUserRequest;
import com.microservices.userservice.dto.UserDTO;
import com.microservices.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test class for UserGraphQLController using Spring for GraphQL test support
 * Compatible with Spring Boot 3.5.6
 */
@GraphQlTest(UserGraphQLController.class)
class UserGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldGetUserById() {
        // Given
        UserDTO mockUser = createMockUser(1L, "john_doe", "john@example.com");
        when(userService.getUserById(1L)).thenReturn(mockUser);

        // When & Then
        String document = """
            query GetUserById($id: ID!) {
                user(id: $id) {
                    id
                    username
                    email
                }
            }
            """;

        graphQlTester.document(document)
                .variable("id", 1L)
                .execute()
                .path("user.id").entity(Long.class).isEqualTo(1L)
                .path("user.username").entity(String.class).isEqualTo("john_doe")
                .path("user.email").entity(String.class).isEqualTo("john@example.com");
    }

    @Test
    void shouldGetUserByUsername() {
        // Given
        UserDTO mockUser = createMockUser(2L, "jane_doe", "jane@example.com");
        when(userService.getUserByUsername("jane_doe")).thenReturn(mockUser);

        // When & Then
        String document = """
            query GetUserByUsername($username: String!) {
                userByUsername(username: $username) {
                    id
                    username
                    email
                }
            }
            """;

        graphQlTester.document(document)
                .variable("username", "jane_doe")
                .execute()
                .path("userByUsername.id").entity(Long.class).isEqualTo(2L)
                .path("userByUsername.username").entity(String.class).isEqualTo("jane_doe")
                .path("userByUsername.email").entity(String.class).isEqualTo("jane@example.com");
    }

    @Test
    void shouldGetAllUsers() {
        // Given
        List<UserDTO> mockUsers = List.of(
                createMockUser(1L, "user1", "user1@example.com"),
                createMockUser(2L, "user2", "user2@example.com")
        );
        when(userService.getAllUsers()).thenReturn(mockUsers);

        // When & Then
        String document = """
            query {
                users {
                    id
                    username
                    email
                }
            }
            """;

        graphQlTester.document(document)
                .execute()
                .path("users").entityList(UserDTO.class).hasSize(2)
                .path("users[0].username").entity(String.class).isEqualTo("user1")
                .path("users[1].username").entity(String.class).isEqualTo("user2");
    }

    @Test
    void shouldCreateUser() {
        // Given
        UserDTO mockUser = createMockUser(3L, "new_user", "new@example.com");
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(mockUser);

        // When & Then
        String document = """
            mutation CreateUser($input: CreateUserInput!) {
                createUser(input: $input) {
                    id
                    username
                    email
                    active
                }
            }
            """;

        graphQlTester.document(document)
                .variable("input", createUserInput())
                .execute()
                .path("createUser.id").entity(Long.class).isEqualTo(3L)
                .path("createUser.username").entity(String.class).isEqualTo("new_user")
                .path("createUser.email").entity(String.class).isEqualTo("new@example.com")
                .path("createUser.active").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    void shouldUpdateUser() {
        // Given
        UserDTO updatedUser = createMockUser(1L, "john_doe", "updated@example.com");
        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(updatedUser);

        // When & Then
        String document = """
            mutation UpdateUser($id: ID!, $input: UpdateUserInput!) {
                updateUser(id: $id, input: $input) {
                    id
                    username
                    email
                }
            }
            """;

        graphQlTester.document(document)
                .variable("id", 1L)
                .variable("input", updateUserInput())
                .execute()
                .path("updateUser.id").entity(Long.class).isEqualTo(1L)
                .path("updateUser.email").entity(String.class).isEqualTo("updated@example.com");
    }

    @Test
    void shouldDeleteUser() {
        // No need to stub since method returns void

        String document = """
            mutation DeleteUser($id: ID!) {
                deleteUser(id: $id)
            }
            """;

        graphQlTester.document(document)
                .variable("id", 1L)
                .execute()
                .path("deleteUser").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    void shouldHandleUserNotFound() {
        when(userService.getUserById(999L))
                .thenThrow(new RuntimeException("User not found with id: 999"));

        String document = """
            query GetUserById($id: ID!) {
                user(id: $id) {
                    id
                    username
                }
            }
            """;

        graphQlTester.document(document)
                .variable("id", 999L)
                .execute()
                .errors()
                .expect(error -> Objects.requireNonNull(error.getMessage())
                        .contains("User not found"));
    }

    // === Helpers ===

    private UserDTO createMockUser(Long id, String username, String email) {
        return new UserDTO(
                id,
                username,
                email,
                "First",
                "Last",
                "+1234567890",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private java.util.Map<String, Object> createUserInput() {
        return java.util.Map.of(
                "username", "new_user",
                "email", "new@example.com",
                "password", "password123",
                "firstName", "New",
                "lastName", "User",
                "phone", "+1234567890"
        );
    }

    private java.util.Map<String, Object> updateUserInput() {
        return java.util.Map.of(
                "email", "updated@example.com",
                "firstName", "Updated",
                "lastName", "Name"
        );
    }
}
