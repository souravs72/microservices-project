package com.microservices.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotificationEvent {
    private String orderId;
    private Long userId;
    private String status;
    private String message;
}
