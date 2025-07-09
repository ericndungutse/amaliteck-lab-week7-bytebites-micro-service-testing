package com.ndungutse.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class OrderRequest {
    private Long restaurantId;
    private String description;
    private Double totalAmount;
}