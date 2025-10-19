package com.microservices.inventoryservice.service;

import com.microservices.inventoryservice.entity.InventoryTransaction;
import com.microservices.inventoryservice.entity.Product;
import com.microservices.inventoryservice.repository.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryTransactionService {
    
    private final InventoryTransactionRepository inventoryTransactionRepository;
    
    @Transactional
    public InventoryTransaction createTransaction(Product product, 
                                                InventoryTransaction.TransactionType transactionType,
                                                Integer quantity,
                                                String notes,
                                                String performedBy,
                                                String referenceId,
                                                String referenceType) {
        log.info("Creating inventory transaction for product: {} type: {} quantity: {}", 
                product.getSku(), transactionType, quantity);
        
        InventoryTransaction transaction = InventoryTransaction.builder()
                .product(product)
                .transactionType(transactionType)
                .quantity(quantity)
                .previousQuantity(product.getQuantityInStock())
                .newQuantity(calculateNewQuantity(product.getQuantityInStock(), quantity, transactionType))
                .referenceId(referenceId)
                .referenceType(referenceType)
                .notes(notes)
                .performedBy(performedBy)
                .build();
        
        InventoryTransaction savedTransaction = inventoryTransactionRepository.save(transaction);
        
        log.info("Inventory transaction created: {}", savedTransaction.getId());
        
        return savedTransaction;
    }
    
    @Transactional(readOnly = true)
    public List<InventoryTransaction> getTransactionsByProduct(Product product) {
        return inventoryTransactionRepository.findByProduct(product);
    }
    
    @Transactional(readOnly = true)
    public Page<InventoryTransaction> getTransactionsByProduct(Product product, Pageable pageable) {
        return inventoryTransactionRepository.findByProduct(product, pageable);
    }
    
    @Transactional(readOnly = true)
    public List<InventoryTransaction> getTransactionsByType(InventoryTransaction.TransactionType transactionType) {
        return inventoryTransactionRepository.findByTransactionType(transactionType);
    }
    
    @Transactional(readOnly = true)
    public Page<InventoryTransaction> getTransactionsByType(InventoryTransaction.TransactionType transactionType, Pageable pageable) {
        return inventoryTransactionRepository.findByTransactionType(transactionType, pageable);
    }
    
    @Transactional(readOnly = true)
    public List<InventoryTransaction> getTransactionsByReference(String referenceId, String referenceType) {
        return inventoryTransactionRepository.findByReferenceIdAndReferenceType(referenceId, referenceType);
    }
    
    private Integer calculateNewQuantity(Integer currentQuantity, Integer transactionQuantity, InventoryTransaction.TransactionType transactionType) {
        switch (transactionType) {
            case IN:
            case RETURN:
                return currentQuantity + transactionQuantity;
            case OUT:
            case DAMAGED:
            case EXPIRED:
                return currentQuantity - transactionQuantity;
            case ADJUSTMENT:
                return transactionQuantity; // For adjustments, quantity is the new value
            case TRANSFER:
                return currentQuantity - transactionQuantity; // Assuming transfer out
            default:
                return currentQuantity;
        }
    }
}

