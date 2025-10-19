package com.microservices.inventoryservice.repository;

import com.microservices.inventoryservice.entity.InventoryTransaction;
import com.microservices.inventoryservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    
    List<InventoryTransaction> findByProduct(Product product);
    
    Page<InventoryTransaction> findByProduct(Product product, Pageable pageable);
    
    List<InventoryTransaction> findByTransactionType(InventoryTransaction.TransactionType transactionType);
    
    Page<InventoryTransaction> findByTransactionType(InventoryTransaction.TransactionType transactionType, Pageable pageable);
    
    List<InventoryTransaction> findByReferenceIdAndReferenceType(String referenceId, String referenceType);
    
    @Query("SELECT it FROM InventoryTransaction it WHERE it.createdAt BETWEEN :startDate AND :endDate")
    List<InventoryTransaction> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT it FROM InventoryTransaction it WHERE it.product = :product AND it.createdAt BETWEEN :startDate AND :endDate")
    List<InventoryTransaction> findByProductAndCreatedAtBetween(@Param("product") Product product,
                                                              @Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT it FROM InventoryTransaction it WHERE it.transactionType = :transactionType AND it.createdAt BETWEEN :startDate AND :endDate")
    List<InventoryTransaction> findByTransactionTypeAndCreatedAtBetween(@Param("transactionType") InventoryTransaction.TransactionType transactionType,
                                                                       @Param("startDate") LocalDateTime startDate,
                                                                       @Param("endDate") LocalDateTime endDate);
}

