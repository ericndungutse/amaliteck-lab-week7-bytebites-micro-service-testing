package com.ndungutse.restaurant_service.mapper;

import com.ndungutse.restaurant_service.dto.RestaurantDto;
import com.ndungutse.restaurant_service.dto.RestaurantRequestDto;
import com.ndungutse.restaurant_service.model.Restaurant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RestaurantMapper {

    // Convert Entity to DTO
    public RestaurantDto toDto(Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }
        return new RestaurantDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getOwner());
    }

    // Convert DTO to Entity
    public Restaurant toEntity(RestaurantDto restaurantDto) {
        if (restaurantDto == null) {
            return null;
        }
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantDto.getId());
        restaurant.setName(restaurantDto.getName());
        restaurant.setOwner(restaurantDto.getOwner());
        return restaurant;
    }

    // Convert Request DTO to Entity
    public Restaurant toEntity(RestaurantRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        return new Restaurant(requestDto.getName(), requestDto.getOwner());
    }

    // Convert List of Entities to List of DTOs
    public List<RestaurantDto> toDtoList(List<Restaurant> restaurants) {
        return restaurants.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Update existing entity with request DTO data
    public void updateEntityFromRequestDto(Restaurant restaurant, RestaurantRequestDto requestDto) {
        if (restaurant != null && requestDto != null) {
            restaurant.setName(requestDto.getName());
            restaurant.setOwner(requestDto.getOwner());
        }
    }
}
