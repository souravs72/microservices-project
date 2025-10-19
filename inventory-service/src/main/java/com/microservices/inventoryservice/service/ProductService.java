package com.microservices.inventoryservice.service;

import com.microservices.inventoryservice.dto.ProductRequest;
import com.microservices.inventoryservice.dto.ProductResponse;
import com.microservices.inventoryservice.entity.Product;
import com.microservices.inventoryservice.entity.InventoryTransaction;
import com.microservices.inventoryservice.repository.ProductRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final InventoryTransactionService inventoryTransactionService;
    private final InventoryEventPublisher inventoryEventPublisher;
    
    @Transactional
    @CircuitBreaker(name = "productService", fallbackMethod = "createProductFallback")
    @Retry(name = "productService")
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.info("Creating product: {}", productRequest.getName());
        
        // Check if SKU already exists
        if (productRepository.findBySku(productRequest.getSku()).isPresent()) {
            throw new RuntimeException("Product with SKU " + productRequest.getSku() + " already exists");
        }
        
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .sku(productRequest.getSku())
                .price(productRequest.getPrice())
                .quantityInStock(productRequest.getQuantityInStock())
                .minStockLevel(productRequest.getMinStockLevel())
                .maxStockLevel(productRequest.getMaxStockLevel())
                .category(productRequest.getCategory())
                .brand(productRequest.getBrand())
                .weight(productRequest.getWeight())
                .dimensions(productRequest.getDimensions())
                .isActive(productRequest.getIsActive())
                .createdBy(productRequest.getCreatedBy())
                .build();
        
        Product savedProduct = productRepository.save(product);
        
        // Create initial inventory transaction
        if (savedProduct.getQuantityInStock() > 0) {
            inventoryTransactionService.createTransaction(
                    savedProduct,
                    InventoryTransaction.TransactionType.IN,
                    savedProduct.getQuantityInStock(),
                    "Initial stock",
                    "SYSTEM",
                    null,
                    null
            );
        }
        
        // Publish product created event
        inventoryEventPublisher.publishProductCreatedEvent(savedProduct);
        
        log.info("Product created successfully: {}", savedProduct.getSku());
        
        return mapToProductResponse(savedProduct);
    }
    
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        return mapToProductResponse(product);
    }
    
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
        
        return mapToProductResponse(product);
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::mapToProductResponse);
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProducts() {
        List<Product> products = productRepository.findByIsActive(true);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponse> getActiveProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByIsActive(true, pageable);
        return products.map(this::mapToProductResponse);
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        List<Product> products = productRepository.findLowStockProducts();
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getOutOfStockProducts() {
        List<Product> products = productRepository.findOutOfStockProducts();
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        log.info("Updating product: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        // Check if SKU is being changed and if it already exists
        if (!product.getSku().equals(productRequest.getSku()) && 
            productRepository.findBySku(productRequest.getSku()).isPresent()) {
            throw new RuntimeException("Product with SKU " + productRequest.getSku() + " already exists");
        }
        
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setSku(productRequest.getSku());
        product.setPrice(productRequest.getPrice());
        product.setMinStockLevel(productRequest.getMinStockLevel());
        product.setMaxStockLevel(productRequest.getMaxStockLevel());
        product.setCategory(productRequest.getCategory());
        product.setBrand(productRequest.getBrand());
        product.setWeight(productRequest.getWeight());
        product.setDimensions(productRequest.getDimensions());
        product.setIsActive(productRequest.getIsActive());
        product.setUpdatedBy(productRequest.getCreatedBy());
        
        Product savedProduct = productRepository.save(product);
        
        // Publish product updated event
        inventoryEventPublisher.publishProductUpdatedEvent(savedProduct);
        
        return mapToProductResponse(savedProduct);
    }
    
    @Transactional
    public ProductResponse updateStock(Long id, Integer newQuantity, String reason, String performedBy) {
        log.info("Updating stock for product: {} to {}", id, newQuantity);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        Integer previousQuantity = product.getQuantityInStock();
        product.setQuantityInStock(newQuantity);
        
        Product savedProduct = productRepository.save(product);
        
        // Create inventory transaction
        InventoryTransaction.TransactionType transactionType = newQuantity > previousQuantity ? 
                InventoryTransaction.TransactionType.IN : InventoryTransaction.TransactionType.OUT;
        
        inventoryTransactionService.createTransaction(
                savedProduct,
                transactionType,
                Math.abs(newQuantity - previousQuantity),
                reason,
                performedBy,
                null,
                null
        );
        
        // Publish stock updated event
        inventoryEventPublisher.publishStockUpdatedEvent(savedProduct, previousQuantity, newQuantity);
        
        return mapToProductResponse(savedProduct);
    }
    
    @Transactional
    public boolean checkProductAvailability(Long productId, Integer quantity) {
        log.info("Checking availability for product: {} quantity: {}", productId, quantity);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        boolean available = product.getIsActive() && product.getQuantityInStock() >= quantity;
        
        log.info("Product availability check result: {} for product: {} quantity: {}", available, productId, quantity);
        
        return available;
    }
    
    @Transactional
    public ProductResponse reserveStock(Long productId, Integer quantity, String referenceId, String referenceType) {
        log.info("Reserving stock for product: {} quantity: {}", productId, quantity);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        if (!product.getIsActive()) {
            throw new RuntimeException("Product is not active");
        }
        
        if (product.getQuantityInStock() < quantity) {
            throw new RuntimeException("Insufficient stock available");
        }
        
        Integer newQuantity = product.getQuantityInStock() - quantity;
        product.setQuantityInStock(newQuantity);
        
        Product savedProduct = productRepository.save(product);
        
        // Create inventory transaction
        inventoryTransactionService.createTransaction(
                savedProduct,
                InventoryTransaction.TransactionType.OUT,
                quantity,
                "Stock reserved for " + referenceType + ": " + referenceId,
                "SYSTEM",
                referenceId,
                referenceType
        );
        
        // Publish stock reserved event
        inventoryEventPublisher.publishStockReservedEvent(savedProduct, quantity, referenceId, referenceType);
        
        return mapToProductResponse(savedProduct);
    }
    
    @Transactional
    public ProductResponse releaseStock(Long productId, Integer quantity, String referenceId, String referenceType) {
        log.info("Releasing stock for product: {} quantity: {}", productId, quantity);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        Integer newQuantity = product.getQuantityInStock() + quantity;
        product.setQuantityInStock(newQuantity);
        
        Product savedProduct = productRepository.save(product);
        
        // Create inventory transaction
        inventoryTransactionService.createTransaction(
                savedProduct,
                InventoryTransaction.TransactionType.RETURN,
                quantity,
                "Stock released from " + referenceType + ": " + referenceId,
                "SYSTEM",
                referenceId,
                referenceType
        );
        
        // Publish stock released event
        inventoryEventPublisher.publishStockReleasedEvent(savedProduct, quantity, referenceId, referenceType);
        
        return mapToProductResponse(savedProduct);
    }
    
    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice())
                .quantityInStock(product.getQuantityInStock())
                .minStockLevel(product.getMinStockLevel())
                .maxStockLevel(product.getMaxStockLevel())
                .category(product.getCategory())
                .brand(product.getBrand())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .build();
    }
    
    // Fallback methods
    public ProductResponse createProductFallback(ProductRequest productRequest, Exception ex) {
        log.error("Product creation failed, using fallback", ex);
        throw new RuntimeException("Product service temporarily unavailable");
    }
}
