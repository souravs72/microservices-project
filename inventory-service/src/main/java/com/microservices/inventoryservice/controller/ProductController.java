package com.microservices.inventoryservice.controller;

import com.microservices.inventoryservice.dto.ProductRequest;
import com.microservices.inventoryservice.dto.ProductResponse;
import com.microservices.inventoryservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Management", description = "APIs for managing inventory and products")
public class ProductController {
    
    private final ProductService productService;
    
    @PostMapping("/products")
    @Operation(summary = "Create a new product", description = "Creates a new product in the inventory")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        log.info("Creating product: {}", productRequest.getName());
        ProductResponse productResponse = productService.createProduct(productRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
    }
    
    @GetMapping("/products/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a product by its ID")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        log.info("Getting product by ID: {}", id);
        ProductResponse productResponse = productService.getProductById(id);
        return ResponseEntity.ok(productResponse);
    }
    
    @GetMapping("/products/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieves a product by its SKU")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        log.info("Getting product by SKU: {}", sku);
        ProductResponse productResponse = productService.getProductBySku(sku);
        return ResponseEntity.ok(productResponse);
    }
    
    @GetMapping("/products")
    @Operation(summary = "Get all products", description = "Retrieves all products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("Getting all products");
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/products/paged")
    @Operation(summary = "Get all products (paginated)", description = "Retrieves paginated products")
    public ResponseEntity<Page<ProductResponse>> getAllProductsPaged(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.info("Getting paginated products");
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/products/active")
    @Operation(summary = "Get active products", description = "Retrieves all active products")
    public ResponseEntity<List<ProductResponse>> getActiveProducts() {
        log.info("Getting active products");
        List<ProductResponse> products = productService.getActiveProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/products/active/paged")
    @Operation(summary = "Get active products (paginated)", description = "Retrieves paginated active products")
    public ResponseEntity<Page<ProductResponse>> getActiveProductsPaged(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.info("Getting paginated active products");
        Page<ProductResponse> products = productService.getActiveProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/products/low-stock")
    @Operation(summary = "Get low stock products", description = "Retrieves products with low stock")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts() {
        log.info("Getting low stock products");
        List<ProductResponse> products = productService.getLowStockProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/products/out-of-stock")
    @Operation(summary = "Get out of stock products", description = "Retrieves products that are out of stock")
    public ResponseEntity<List<ProductResponse>> getOutOfStockProducts() {
        log.info("Getting out of stock products");
        List<ProductResponse> products = productService.getOutOfStockProducts();
        return ResponseEntity.ok(products);
    }
    
    @PutMapping("/products/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest productRequest) {
        log.info("Updating product: {}", id);
        ProductResponse productResponse = productService.updateProduct(id, productRequest);
        return ResponseEntity.ok(productResponse);
    }
    
    @PutMapping("/products/{id}/stock")
    @Operation(summary = "Update product stock", description = "Updates the stock quantity of a product")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestParam String reason,
            @RequestParam String performedBy) {
        log.info("Updating stock for product: {} to {}", id, quantity);
        ProductResponse productResponse = productService.updateStock(id, quantity, reason, performedBy);
        return ResponseEntity.ok(productResponse);
    }
    
    @GetMapping("/products/{productId}/availability")
    @Operation(summary = "Check product availability", description = "Checks if a product is available in the requested quantity")
    public ResponseEntity<Boolean> checkProductAvailability(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        log.info("Checking availability for product: {} quantity: {}", productId, quantity);
        boolean available = productService.checkProductAvailability(productId, quantity);
        return ResponseEntity.ok(available);
    }
    
    @PostMapping("/products/{productId}/reserve")
    @Operation(summary = "Reserve stock", description = "Reserves stock for a product")
    public ResponseEntity<ProductResponse> reserveStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            @RequestParam String referenceId,
            @RequestParam String referenceType) {
        log.info("Reserving stock for product: {} quantity: {}", productId, quantity);
        ProductResponse productResponse = productService.reserveStock(productId, quantity, referenceId, referenceType);
        return ResponseEntity.ok(productResponse);
    }
    
    @PostMapping("/products/{productId}/release")
    @Operation(summary = "Release stock", description = "Releases reserved stock for a product")
    public ResponseEntity<ProductResponse> releaseStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            @RequestParam String referenceId,
            @RequestParam String referenceType) {
        log.info("Releasing stock for product: {} quantity: {}", productId, quantity);
        ProductResponse productResponse = productService.releaseStock(productId, quantity, referenceId, referenceType);
        return ResponseEntity.ok(productResponse);
    }
}

