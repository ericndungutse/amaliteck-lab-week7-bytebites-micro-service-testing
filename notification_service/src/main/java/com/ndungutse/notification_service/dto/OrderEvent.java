package com.ndungutse.notification_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderEvent {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private String description;
    private String status;
    private Double total_amount;
    private LocalDateTime createdAt;
}