package com.microservices.orderservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
import java.util.List;

/**
 * Order entity representing customer orders.
 * Includes comprehensive audit trail and validation.
 */
@Entity
@Table(name = "orders",
       indexes = {
           @Index(name = "idx_orders_order_number", columnList = "order_number"),
           @Index(name = "idx_orders_user_id", columnList = "user_id"),
           @Index(name = "idx_orders_status", columnList = "status"),
           @Index(name = "idx_orders_created_at", columnList = "created_at"),
           @Index(name = "idx_orders_total_amount", columnList = "total_amount")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_orders_order_number", columnNames = "order_number")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @NotBlank(message = "Order number is required")
    @Size(max = 50, message = "Order number must not exceed 50 characters")
    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;
    
    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;
    
    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "subtotal_amount", precision = 10, scale = 2)
    private BigDecimal subtotalAmount;
    
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;
    
    @Column(name = "shipping_amount", precision = 10, scale = 2)
    private BigDecimal shippingAmount;
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;
    
    @Size(max = 500, message = "Shipping address must not exceed 500 characters")
    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;
    
    @Size(max = 500, message = "Billing address must not exceed 500 characters")
    @Column(name = "billing_address", length = 500)
    private String billingAddress;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Size(max = 100, message = "Payment method must not exceed 100 characters")
    @Column(name = "payment_method", length = 100)
    private String paymentMethod;
    
    @Size(max = 100, message = "Payment status must not exceed 100 characters")
    @Column(name = "payment_status", length = 100)
    private String paymentStatus;
    
    @Column(name = "payment_reference", length = 255)
    private String paymentReference;
    
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;
    
    @Column(name = "shipping_carrier", length = 100)
    private String shippingCarrier;
    
    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;
    
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
    
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    @Size(max = 500, message = "Refund reason must not exceed 500 characters")
    @Column(name = "refund_reason", length = 500)
    private String refundReason;

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
     * Check if order can be cancelled
     */
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    /**
     * Check if order can be shipped
     */
    public boolean canBeShipped() {
        return status == OrderStatus.CONFIRMED || status == OrderStatus.PROCESSING;
    }

    /**
     * Check if order can be delivered
     */
    public boolean canBeDelivered() {
        return status == OrderStatus.SHIPPED;
    }

    /**
     * Check if order can be refunded
     */
    public boolean canBeRefunded() {
        return status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED;
    }

    /**
     * Calculate total items count
     */
    public int getTotalItemsCount() {
        return orderItems != null ? orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum() : 0;
    }

    /**
     * Order status enum
     */
    public enum OrderStatus {
        PENDING("Pending"),
        CONFIRMED("Confirmed"),
        PROCESSING("Processing"),
        SHIPPED("Shipped"),
        DELIVERED("Delivered"),
        CANCELLED("Cancelled"),
        REFUNDED("Refunded");

        private final String displayName;

        OrderStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}

