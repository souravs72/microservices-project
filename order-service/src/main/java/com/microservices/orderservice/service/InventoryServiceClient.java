package com.microservices.orderservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "inventory-service", url = "${inventory.service.url}")
public interface InventoryServiceClient {
    
    @GetMapping("/api/inventory/products/{productId}/availability")
    boolean checkProductAvailability(@PathVariable("productId") Long productId, 
                                   @RequestHeader("X-Quantity") Integer quantity,
                                   @RequestHeader("X-Internal-API-Key") String apiKey);
}

