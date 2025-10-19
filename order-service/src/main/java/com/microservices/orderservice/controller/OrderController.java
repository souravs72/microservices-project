package com.microservices.orderservice.controller;

import com.microservices.orderservice.dto.OrderRequest;
import com.microservices.orderservice.dto.OrderResponse;
import com.microservices.orderservice.entity.Order;
import com.microservices.orderservice.service.OrderService;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {
    
    private final OrderService orderService;
    
    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieves all orders with pagination and filtering")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @Parameter(description = "Pagination parameters") Pageable pageable,
            @RequestParam(required = false) String status) {
        log.info("Getting all orders with page: {}, size: {}, status: {}", pageable.getPageNumber(), pageable.getPageSize(), status);
        Page<OrderResponse> orders = orderService.getAllOrders(pageable, status);
        return ResponseEntity.ok(orders);
    }
    
    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order with the provided details")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        log.info("Creating order for user: {}", orderRequest.getUserId());
        OrderResponse orderResponse = orderService.createOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves an order by its ID")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        log.info("Getting order by ID: {}", id);
        OrderResponse orderResponse = orderService.getOrderById(id);
        return ResponseEntity.ok(orderResponse);
    }
    
    @GetMapping("/order-number/{orderNumber}")
    @Operation(summary = "Get order by order number", description = "Retrieves an order by its order number")
    public ResponseEntity<OrderResponse> getOrderByOrderNumber(@PathVariable String orderNumber) {
        log.info("Getting order by order number: {}", orderNumber);
        OrderResponse orderResponse = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(orderResponse);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get orders by user ID", description = "Retrieves all orders for a specific user")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable Long userId) {
        log.info("Getting orders for user: {}", userId);
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/user/{userId}/paged")
    @Operation(summary = "Get orders by user ID (paginated)", description = "Retrieves paginated orders for a specific user")
    public ResponseEntity<Page<OrderResponse>> getOrdersByUserIdPaged(
            @PathVariable Long userId,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.info("Getting paginated orders for user: {}", userId);
        Page<OrderResponse> orders = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Updates the status of an order")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        log.info("Updating order status for order ID: {} to {}", id, status);
        OrderResponse orderResponse = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(orderResponse);
    }
    
    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels an order with optional reason")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        log.info("Cancelling order ID: {} with reason: {}", id, reason);
        OrderResponse orderResponse = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(orderResponse);
    }
}

