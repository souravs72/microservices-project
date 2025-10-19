package com.microservices.auth.client;

import com.microservices.auth.dto.CreateUserProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserServiceClient {
    
    @PostMapping("/api/users")
    void createUserProfile(@RequestBody CreateUserProfileRequest request, 
                          @RequestHeader("X-Internal-API-Key") String apiKey);
}
