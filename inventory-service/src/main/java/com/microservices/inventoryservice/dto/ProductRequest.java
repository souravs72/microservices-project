package com.microservices.inventoryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    
    @NotBlank(message = "Product name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Product SKU is required")
    private String sku;
    
    @NotNull(message = "Product price is required")
    @PositiveOrZero(message = "Product price must be positive or zero")
    private BigDecimal price;
    
    @NotNull(message = "Quantity in stock is required")
    @PositiveOrZero(message = "Quantity in stock must be positive or zero")
    private Integer quantityInStock;
    
    @NotNull(message = "Minimum stock level is required")
    @PositiveOrZero(message = "Minimum stock level must be positive or zero")
    private Integer minStockLevel;
    
    private Integer maxStockLevel;
    
    private String category;
    
    private String brand;
    
    private BigDecimal weight;
    
    private String dimensions;
    
    private Boolean isActive = true;
    
    private String createdBy;
}

