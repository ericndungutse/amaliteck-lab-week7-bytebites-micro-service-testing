package com.ndungutse.restaurant_service.controller;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ndungutse.restaurant_service.dto.RestaurantDto;
import com.ndungutse.restaurant_service.dto.RestaurantRequestDto;
import com.ndungutse.restaurant_service.service.RestaurantService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    private final RestTemplate restTemplate;

    private static final String RESTAURANT_SERVICE_URL = "http://localhost:9000/api/v1/orders/resilience-checker";

    // Resilience checker endpoint
    @GetMapping("/resilience-checker")
    public ResponseEntity<String> resilienceChecker() {
        ResponseEntity<String> response = restTemplate.getForEntity(RESTAURANT_SERVICE_URL,
                String.class);
        return new ResponseEntity<>(response.getBody() + " From Order Service", HttpStatus.OK);
    }

    // Create a new restaurant
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestaurantDto> createRestaurant(@Valid @RequestBody RestaurantRequestDto requestDto) {
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
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<RestaurantDto>> getMyRestaurants() {
        List<RestaurantDto> restaurants = restaurantService.getMyRestaurants();
        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }


    // Get a restaurant by ID
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto> getRestaurantById(@PathVariable Long id) {
        Optional<RestaurantDto> restaurant = restaurantService.getRestaurantById(id);
        return restaurant.map(r -> new ResponseEntity<>(r, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Update a restaurant
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantDto> updateRestaurant(@PathVariable Long id,
          @Valid  @RequestBody RestaurantRequestDto requestDto) {

            RestaurantDto updatedRestaurant = restaurantService.updateRestaurant(id, requestDto);
            return new ResponseEntity<>(updatedRestaurant, HttpStatus.OK);

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

    // Check if a restaurant exists
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        boolean exists = restaurantService.existsById(id);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    // Get a total count of restaurants
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalCount() {
        long count = restaurantService.getAllRestaurants().size();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}
