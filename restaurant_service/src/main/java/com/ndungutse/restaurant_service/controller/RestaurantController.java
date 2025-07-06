package com.ndungutse.restaurant_service.controller;

import com.ndungutse.restaurant_service.dto.RestaurantDto;
import com.ndungutse.restaurant_service.dto.RestaurantRequestDto;
import com.ndungutse.restaurant_service.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // Create a new restaurant
    @PostMapping
    public ResponseEntity<RestaurantDto> createRestaurant(@RequestBody RestaurantRequestDto requestDto) {
        RestaurantDto createdRestaurant = restaurantService.createRestaurant(requestDto);
        return new ResponseEntity<>(createdRestaurant, HttpStatus.CREATED);
    }

    // Get all restaurants
    @GetMapping
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants() {
        List<RestaurantDto> restaurants = restaurantService.getAllRestaurants();
        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }

    // Get current user's restaurants
    @GetMapping("/my-restaurants")
    public ResponseEntity<List<RestaurantDto>> getMyRestaurants() {
        List<RestaurantDto> restaurants = restaurantService.getMyRestaurants();
        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }

    // Get restaurant by ID
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto> getRestaurantById(@PathVariable Long id) {
        Optional<RestaurantDto> restaurant = restaurantService.getRestaurantById(id);
        return restaurant.map(r -> new ResponseEntity<>(r, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Update restaurant
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantDto> updateRestaurant(@PathVariable Long id,
            @RequestBody RestaurantRequestDto requestDto) {
        try {
            RestaurantDto updatedRestaurant = restaurantService.updateRestaurant(id, requestDto);
            return new ResponseEntity<>(updatedRestaurant, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Delete restaurant
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        try {
            restaurantService.deleteRestaurant(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Check if restaurant exists
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        boolean exists = restaurantService.existsById(id);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    // Get total count of restaurants
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalCount() {
        long count = restaurantService.getAllRestaurants().size();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}
