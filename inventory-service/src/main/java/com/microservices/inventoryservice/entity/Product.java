package com.microservices.inventoryservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 255)
    @NotBlank(message = "Product name is required")
    private String name;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "sku", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Product SKU is required")
    private String sku;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Product price is required")
    @PositiveOrZero(message = "Product price must be positive or zero")
    private BigDecimal price;
    
    @Column(name = "quantity_in_stock", nullable = false)
    @NotNull(message = "Quantity in stock is required")
    @PositiveOrZero(message = "Quantity in stock must be positive or zero")
    private Integer quantityInStock;
    
    @Column(name = "min_stock_level", nullable = false)
    @NotNull(message = "Minimum stock level is required")
    @PositiveOrZero(message = "Minimum stock level must be positive or zero")
    private Integer minStockLevel;
    
    @Column(name = "max_stock_level")
    private Integer maxStockLevel;
    
    @Column(name = "category", length = 100)
    private String category;
    
    @Column(name = "brand", length = 100)
    private String brand;
    
    @Column(name = "weight")
    private BigDecimal weight;
    
    @Column(name = "dimensions", length = 100)
    private String dimensions;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}

