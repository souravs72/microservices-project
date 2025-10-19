package com.microservices.inventoryservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;
    
    @Column(name = "quantity", nullable = false)
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    @Column(name = "previous_quantity", nullable = false)
    @NotNull(message = "Previous quantity is required")
    private Integer previousQuantity;
    
    @Column(name = "new_quantity", nullable = false)
    @NotNull(message = "New quantity is required")
    private Integer newQuantity;
    
    @Column(name = "reference_id", length = 100)
    private String referenceId; // Order ID, Return ID, etc.
    
    @Column(name = "reference_type", length = 50)
    private String referenceType; // ORDER, RETURN, ADJUSTMENT, etc.
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Column(name = "performed_by", length = 100)
    private String performedBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum TransactionType {
        IN,           // Stock in
        OUT,          // Stock out
        ADJUSTMENT,   // Manual adjustment
        RETURN,       // Return to stock
        TRANSFER,     // Transfer between locations
        DAMAGED,      // Damaged goods
        EXPIRED       // Expired goods
    }
}

