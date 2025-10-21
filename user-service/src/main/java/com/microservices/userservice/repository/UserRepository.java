package com.microservices.userservice.repository;

import com.microservices.userservice.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.createdAt > :since OR u.updatedAt > :since ORDER BY u.createdAt ASC")
    List<User> findUsersForReconciliation(LocalDateTime since, Pageable pageable);
}
