package com.ndungutse.restaurant_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRequestDto {
    @NotNull(message = "Restaurant name cannot be null")
    @NotBlank(message = "Restaurant should have alphanumerical characters")
    @Size(min = 5, max = 50, message = "Restaurant name should be at least 5 characters and 50 characters at most.")
    private String name;
    @NotNull(message = "Owner cannot be null")
    private Long owner;
}
