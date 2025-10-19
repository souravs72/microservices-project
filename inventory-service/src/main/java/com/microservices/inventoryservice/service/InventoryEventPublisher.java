package com.microservices.inventoryservice.service;

import com.microservices.inventoryservice.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishProductCreatedEvent(Product product) {
        try {
            log.info("Publishing product created event for product: {}", product.getSku());
            kafkaTemplate.send("inventory-events", "product.created", product);
        } catch (Exception e) {
            log.error("Failed to publish product created event", e);
        }
    }
    
    public void publishProductUpdatedEvent(Product product) {
        try {
            log.info("Publishing product updated event for product: {}", product.getSku());
            kafkaTemplate.send("inventory-events", "product.updated", product);
        } catch (Exception e) {
            log.error("Failed to publish product updated event", e);
        }
    }
    
    public void publishStockUpdatedEvent(Product product, Integer previousQuantity, Integer newQuantity) {
        try {
            log.info("Publishing stock updated event for product: {} from {} to {}", 
                    product.getSku(), previousQuantity, newQuantity);
            kafkaTemplate.send("inventory-events", "stock.updated", product);
        } catch (Exception e) {
            log.error("Failed to publish stock updated event", e);
        }
    }
    
    public void publishStockReservedEvent(Product product, Integer quantity, String referenceId, String referenceType) {
        try {
            log.info("Publishing stock reserved event for product: {} quantity: {}", 
                    product.getSku(), quantity);
            kafkaTemplate.send("inventory-events", "stock.reserved", product);
        } catch (Exception e) {
            log.error("Failed to publish stock reserved event", e);
        }
    }
    
    public void publishStockReleasedEvent(Product product, Integer quantity, String referenceId, String referenceType) {
        try {
            log.info("Publishing stock released event for product: {} quantity: {}", 
                    product.getSku(), quantity);
            kafkaTemplate.send("inventory-events", "stock.released", product);
        } catch (Exception e) {
            log.error("Failed to publish stock released event", e);
        }
    }
    
    public void publishLowStockEvent(Product product) {
        try {
            log.info("Publishing low stock event for product: {}", product.getSku());
            kafkaTemplate.send("inventory-events", "stock.low", product);
        } catch (Exception e) {
            log.error("Failed to publish low stock event", e);
        }
    }
    
    public void publishOutOfStockEvent(Product product) {
        try {
            log.info("Publishing out of stock event for product: {}", product.getSku());
            kafkaTemplate.send("inventory-events", "stock.out", product);
        } catch (Exception e) {
            log.error("Failed to publish out of stock event", e);
        }
    }
}

