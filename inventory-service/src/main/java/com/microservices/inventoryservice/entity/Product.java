package com.microservices.inventoryservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product entity representing inventory items.
 * Includes comprehensive audit trail and validation.
 */
@Entity
@Table(name = "products",
       indexes = {
           @Index(name = "idx_products_sku", columnList = "sku"),
           @Index(name = "idx_products_name", columnList = "name"),
           @Index(name = "idx_products_category", columnList = "category"),
           @Index(name = "idx_products_brand", columnList = "brand"),
           @Index(name = "idx_products_is_active", columnList = "is_active"),
           @Index(name = "idx_products_quantity_in_stock", columnList = "quantity_in_stock"),
           @Index(name = "idx_products_created_at", columnList = "created_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_products_sku", columnNames = "sku")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;
    
    @NotBlank(message = "Product SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    @Column(name = "sku", unique = true, nullable = false, length = 100)
    private String sku;
    
    @NotNull(message = "Product price is required")
    @PositiveOrZero(message = "Product price must be positive or zero")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @NotNull(message = "Quantity in stock is required")
    @PositiveOrZero(message = "Quantity in stock must be positive or zero")
    @Column(name = "quantity_in_stock", nullable = false)
    private Integer quantityInStock;
    
    @NotNull(message = "Minimum stock level is required")
    @PositiveOrZero(message = "Minimum stock level must be positive or zero")
    @Column(name = "min_stock_level", nullable = false)
    private Integer minStockLevel;
    
    @PositiveOrZero(message = "Maximum stock level must be positive or zero")
    @Column(name = "max_stock_level")
    private Integer maxStockLevel;
    
    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Column(name = "category", length = 100)
    private String category;
    
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    @Column(name = "brand", length = 100)
    private String brand;
    
    @PositiveOrZero(message = "Weight must be positive or zero")
    @Column(name = "weight", precision = 8, scale = 3)
    private BigDecimal weight;
    
    @Size(max = 100, message = "Dimensions must not exceed 100 characters")
    @Column(name = "dimensions", length = 100)
    private String dimensions;
    
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Size(max = 1000, message = "Tags must not exceed 1000 characters")
    @Column(name = "tags", length = 1000)
    private String tags;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
    
    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint = 0;
    
    @Column(name = "supplier_id")
    private Long supplierId;
    
    @Column(name = "supplier_sku", length = 100)
    private String supplierSku;
    
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;
    
    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;
    
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;
    
    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;
    
    @Column(name = "warranty_period_months")
    private Integer warrantyPeriodMonths;
    
    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;
    
    @Column(name = "last_sold_at")
    private LocalDateTime lastSoldAt;
    
    @Column(name = "total_sold", nullable = false)
    private Integer totalSold = 0;
    
    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_from_ip", length = 45, updatable = false)
    private String createdFromIp;

    @Column(name = "updated_from_ip", length = 45)
    private String updatedFromIp;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if product is low in stock
     */
    public boolean isLowStock() {
        return quantityInStock <= minStockLevel;
    }

    /**
     * Check if product is out of stock
     */
    public boolean isOutOfStock() {
        return quantityInStock <= 0;
    }

    /**
     * Check if product needs reordering
     */
    public boolean needsReorder() {
        return quantityInStock <= reorderPoint;
    }

    /**
     * Check if product is available for sale
     */
    public boolean isAvailableForSale() {
        return isActive && status == ProductStatus.ACTIVE && !isOutOfStock();
    }

    /**
     * Calculate current selling price (considering discounts)
     */
    public BigDecimal getCurrentPrice() {
        if (salePrice != null) {
            return salePrice;
        }
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            return price.multiply(BigDecimal.ONE.subtract(discountPercentage.divide(BigDecimal.valueOf(100))));
        }
        return price;
    }

    /**
     * Product status enum
     */
    public enum ProductStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        DISCONTINUED("Discontinued"),
        OUT_OF_STOCK("Out of Stock"),
        COMING_SOON("Coming Soon");

        private final String displayName;

        ProductStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}

