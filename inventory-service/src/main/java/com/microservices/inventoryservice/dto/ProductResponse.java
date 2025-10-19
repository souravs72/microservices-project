package com.microservices.inventoryservice.dto;

import com.microservices.inventoryservice.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private Long id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private Integer quantityInStock;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private String category;
    private String brand;
    private BigDecimal weight;
    private String dimensions;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}

