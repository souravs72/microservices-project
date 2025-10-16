package com.microservices.userservice.controller;

import com.microservices.userservice.dto.CreateUserRequest;
import com.microservices.userservice.dto.UpdateUserRequest;
import com.microservices.userservice.dto.UserDTO;
import com.microservices.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserGraphQLController {

    private final UserService userService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public UserDTO user(@Argument("id") Long id) {
        log.debug("GraphQL query: user with id {}", id);
        return userService.getUserById(id);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public UserDTO userByUsername(@Argument("username") String username) {
        log.debug("GraphQL query: userByUsername with username {}", username);
        return userService.getUserByUsername(username);
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> users() {
        log.debug("GraphQL query: all users");
        return userService.getAllUsers();
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO createUser(@Argument("input") CreateUserRequest input) {
        log.debug("GraphQL mutation: createUser with username {}", input.getUsername());
        return userService.createUser(input);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public UserDTO updateUser(@Argument("id") Long id,
                              @Argument("input") UpdateUserRequest input) {
        log.debug("GraphQL mutation: updateUser with id {}", id);
        return userService.updateUser(id, input);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteUser(@Argument("id") Long id) {
        log.debug("GraphQL mutation: deleteUser with id {}", id);
        userService.deleteUser(id);
        return true;
    }
}