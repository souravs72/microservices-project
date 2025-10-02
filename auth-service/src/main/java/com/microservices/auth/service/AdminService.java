package com.microservices.auth.service;

import com.microservices.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional
    public boolean unlockUserAccount(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    if (user.getAccountLocked()) {
                        user.setAccountLocked(false);
                        user.setAccountLockedAt(null);
                        user.setFailedLoginAttempts(0);
                        user.setLastFailedLoginAt(null);
                        userRepository.save(user);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    public Map<String, Object> getAccountSecurityStatus(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    Map<String, Object> status = new HashMap<>();
                    status.put("username", user.getUsername());
                    status.put("enabled", user.getEnabled());
                    status.put("accountLocked", user.getAccountLocked());
                    status.put("failedLoginAttempts", user.getFailedLoginAttempts());
                    status.put("lastLoginAt", user.getLastLoginAt());
                    status.put("lastLoginIp", user.getLastLoginIp());
                    status.put("lastFailedLoginAt", user.getLastFailedLoginAt());
                    status.put("accountLockedAt", user.getAccountLockedAt());
                    return status;
                })
                .orElse(Map.of("error", "User not found"));
    }
}