package com.microservices.orderservice.service;

import com.microservices.orderservice.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishOrderCreatedEvent(Order order) {
        try {
            log.info("Publishing order created event for order: {}", order.getOrderNumber());
            kafkaTemplate.send("order-events", "order.created", order);
        } catch (Exception e) {
            log.error("Failed to publish order created event", e);
        }
    }
    
    public void publishOrderStatusChangedEvent(Order order, Order.OrderStatus oldStatus) {
        try {
            log.info("Publishing order status changed event for order: {} from {} to {}", 
                    order.getOrderNumber(), oldStatus, order.getStatus());
            kafkaTemplate.send("order-events", "order.status.changed", order);
        } catch (Exception e) {
            log.error("Failed to publish order status changed event", e);
        }
    }
    
    public void publishOrderCancelledEvent(Order order) {
        try {
            log.info("Publishing order cancelled event for order: {}", order.getOrderNumber());
            kafkaTemplate.send("order-events", "order.cancelled", order);
        } catch (Exception e) {
            log.error("Failed to publish order cancelled event", e);
        }
    }
}

