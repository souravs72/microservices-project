package com.microservices.orderservice.service;

import com.microservices.orderservice.dto.OrderRequest;
import com.microservices.orderservice.dto.OrderResponse;
import com.microservices.orderservice.entity.Order;
import com.microservices.orderservice.entity.OrderItem;
import com.microservices.orderservice.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final UserServiceClient userServiceClient;
    private final OrderEventPublisher orderEventPublisher;
    
    @Value("${internal.api.key}")
    private String internalApiKey;
    
    @Transactional
    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    @Retry(name = "orderService")
    public OrderResponse createOrder(OrderRequest orderRequest) {
        log.info("Creating order for user: {}", orderRequest.getUserId());
        
        // Validate user exists
        validateUser(orderRequest.getUserId());
        
        // Check inventory availability
        validateInventory(orderRequest.getOrderItems());
        
        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(orderRequest.getUserId())
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(orderRequest.getShippingAddress())
                .billingAddress(orderRequest.getBillingAddress())
                .notes(orderRequest.getNotes())
                .build();
        
        // Create order items
        List<OrderItem> orderItems = orderRequest.getOrderItems().stream()
                .map(itemRequest -> createOrderItem(order, itemRequest))
                .collect(Collectors.toList());
        
        order.setOrderItems(orderItems);
        
        // Calculate total amount
        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        order.setTotalAmount(totalAmount);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Publish order created event
        orderEventPublisher.publishOrderCreatedEvent(savedOrder);
        
        log.info("Order created successfully: {}", savedOrder.getOrderNumber());
        
        return mapToOrderResponse(savedOrder);
    }
    
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        return mapToOrderResponse(order);
    }
    
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
        
        return mapToOrderResponse(order);
    }
    
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return orders.map(this::mapToOrderResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable, String status) {
        Page<Order> orders;
        if (status != null && !status.isEmpty()) {
            try {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByStatus(orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter: {}", status);
                orders = orderRepository.findAll(pageable);
            }
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.map(this::mapToOrderResponse);
    }
    
    @Transactional
    public OrderResponse updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        
        // Update timestamps based on status
        switch (status) {
            case SHIPPED:
                order.setShippedAt(LocalDateTime.now());
                break;
            case DELIVERED:
                order.setDeliveredAt(LocalDateTime.now());
                break;
            case CANCELLED:
                order.setCancelledAt(LocalDateTime.now());
                break;
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Publish order status changed event
        orderEventPublisher.publishOrderStatusChangedEvent(savedOrder, oldStatus);
        
        return mapToOrderResponse(savedOrder);
    }
    
    @Transactional
    public OrderResponse cancelOrder(Long id, String reason) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled");
        }
        
        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel delivered order");
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);
        
        Order savedOrder = orderRepository.save(order);
        
        // Publish order cancelled event
        orderEventPublisher.publishOrderCancelledEvent(savedOrder);
        
        return mapToOrderResponse(savedOrder);
    }
    
    private void validateUser(Long userId) {
        try {
            userServiceClient.getUserById(userId, internalApiKey);
        } catch (Exception e) {
            log.error("User validation failed for user ID: {}", userId, e);
            throw new RuntimeException("User not found or invalid");
        }
    }
    
    private void validateInventory(List<OrderRequest.OrderItemRequest> orderItems) {
        for (OrderRequest.OrderItemRequest item : orderItems) {
            try {
                boolean available = inventoryServiceClient.checkProductAvailability(item.getProductId(), item.getQuantity(), internalApiKey);
                if (!available) {
                    throw new RuntimeException("Product not available: " + item.getProductId());
                }
            } catch (Exception e) {
                log.error("Inventory validation failed for product ID: {}", item.getProductId(), e);
                throw new RuntimeException("Inventory validation failed");
            }
        }
    }
    
    private OrderItem createOrderItem(Order order, OrderRequest.OrderItemRequest itemRequest) {
        return OrderItem.builder()
                .order(order)
                .productId(itemRequest.getProductId())
                .productName(itemRequest.getProductName())
                .productSku(itemRequest.getProductSku())
                .quantity(itemRequest.getQuantity())
                .unitPrice(itemRequest.getUnitPrice())
                .discountAmount(itemRequest.getDiscountAmount())
                .taxAmount(itemRequest.getTaxAmount())
                .build();
    }
    
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .notes(order.getNotes())
                .orderItems(order.getOrderItems().stream()
                        .map(this::mapToOrderItemResponse)
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .cancellationReason(order.getCancellationReason())
                .build();
    }
    
    private OrderResponse.OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
        return OrderResponse.OrderItemResponse.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .productSku(orderItem.getProductSku())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .discountAmount(orderItem.getDiscountAmount())
                .taxAmount(orderItem.getTaxAmount())
                .build();
    }
    
    // Fallback methods
    public OrderResponse createOrderFallback(OrderRequest orderRequest, Exception ex) {
        log.error("Order creation failed, using fallback", ex);
        throw new RuntimeException("Order service temporarily unavailable");
    }
}
