package com.microservices.inventoryservice.repository;

import com.microservices.inventoryservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    List<Product> findByIsActive(Boolean isActive);
    
    Page<Product> findByIsActive(Boolean isActive, Pageable pageable);
    
    List<Product> findByCategory(String category);
    
    Page<Product> findByCategory(String category, Pageable pageable);
    
    List<Product> findByBrand(String brand);
    
    Page<Product> findByBrand(String brand, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.quantityInStock <= p.minStockLevel AND p.isActive = true")
    List<Product> findLowStockProducts();
    
    @Query("SELECT p FROM Product p WHERE p.quantityInStock = 0 AND p.isActive = true")
    List<Product> findOutOfStockProducts();
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% OR p.description LIKE %:description%")
    List<Product> findByNameOrDescriptionContaining(@Param("name") String name, @Param("description") String description);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% OR p.description LIKE %:description%")
    Page<Product> findByNameOrDescriptionContaining(@Param("name") String name, @Param("description") String description, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true")
    Long countActiveProducts();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantityInStock <= p.minStockLevel AND p.isActive = true")
    Long countLowStockProducts();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantityInStock = 0 AND p.isActive = true")
    Long countOutOfStockProducts();
}

