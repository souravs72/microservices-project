package com.microservices.userservice.controller;

import com.microservices.userservice.dto.CreateUserRequest;
import com.microservices.userservice.dto.UpdateUserRequest;
import com.microservices.userservice.dto.UserDTO;
import com.microservices.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Controller for User operations using Spring for GraphQL
 * Compatible with Spring Boot 3.5.6 and Spring Framework 6.2.x
 */
@Controller
@RequiredArgsConstructor
public class UserGraphQLController {

    private final UserService userService;

    /** Query: Get user by ID */
    @QueryMapping
    public UserDTO user(@Argument("id") Long id) {
        return userService.getUserById(id);
    }

    /** Query: Get user by username */
    @QueryMapping
    public UserDTO userByUsername(@Argument("username") String username) {
        return userService.getUserByUsername(username);
    }

    /** Query: Get all users */
    @QueryMapping
    public List<UserDTO> users() {
        return userService.getAllUsers();
    }

    /** Mutation: Create a new user */
    @MutationMapping
    public UserDTO createUser(@Argument("input") CreateUserRequest input) {
        return userService.createUser(input);
    }

    /** Mutation: Update an existing user */
    @MutationMapping
    public UserDTO updateUser(@Argument("id") Long id,
                              @Argument("input") UpdateUserRequest input) {
        return userService.updateUser(id, input);
    }

    /** Mutation: Delete a user */
    @MutationMapping
    public Boolean deleteUser(@Argument("id") Long id) {
        userService.deleteUser(id);
        return true;
    }
}
