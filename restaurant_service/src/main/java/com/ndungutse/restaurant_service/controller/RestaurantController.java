package com.ndungutse.restaurant_service.controller;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    private final RestaurantService restaurantService;

    private final RestTemplate restTemplate;

    private static final String RESTAURANT_SERVICE_URL = "http://localhost:9000/api/v1/orders/resilience-checker";

    // Resilience checker endpoint
    @GetMapping("/resilience-checker")
    public ResponseEntity<String> resilienceChecker() {
        logger.info("Resilience check initiated");

        ResponseEntity<String> response = restTemplate.getForEntity(RESTAURANT_SERVICE_URL,
                String.class);
        logger.info("Resilience check successful");
        return new ResponseEntity<>(response.getBody() + " From Order Service", HttpStatus.OK);

    }

    // Create a new restaurant
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestaurantDto> createRestaurant(@Valid @RequestBody RestaurantRequestDto requestDto) {
        logger.info("Request received to create restaurant: {}", requestDto.getName());
        RestaurantDto createdRestaurant = restaurantService.createRestaurant(requestDto);
        logger.info("Restaurant created successfully with ID: {}", createdRestaurant.getId());
        return new ResponseEntity<>(createdRestaurant, HttpStatus.CREATED);
    }

    // Get all restaurants
    @GetMapping
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants() {
        logger.info("Request received to get all restaurants");
        List<RestaurantDto> restaurants = restaurantService.getAllRestaurants();
        logger.info("Returning {} restaurants", restaurants.size());
        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }

    // Get current user's restaurants
    @GetMapping("/my-restaurants")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<RestaurantDto>> getMyRestaurants() {
        logger.info("Request received to get current user's restaurants");
        List<RestaurantDto> restaurants = restaurantService.getMyRestaurants();
        logger.info("Returning {} restaurants for current user", restaurants.size());
        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }

    // Get a restaurant by ID
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto> getRestaurantById(@PathVariable Long id) {
        logger.info("Request received to get restaurant with ID: {}", id);
        Optional<RestaurantDto> restaurant = restaurantService.getRestaurantById(id);
        if (restaurant.isPresent()) {
            logger.info("Restaurant found with ID: {}", id);
            return new ResponseEntity<>(restaurant.get(), HttpStatus.OK);
        } else {
            logger.warn("Restaurant not found with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Update a restaurant
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantDto> updateRestaurant(@PathVariable Long id,
            @Valid @RequestBody RestaurantRequestDto requestDto) {
        logger.info("Request received to update restaurant with ID: {}", id);

        try {
            RestaurantDto updatedRestaurant = restaurantService.updateRestaurant(id, requestDto);
            logger.info("Restaurant updated successfully with ID: {}", id);
            return new ResponseEntity<>(updatedRestaurant, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating restaurant with ID: {}, error: {}", id, e.getMessage());
            throw e;
        }
    }

    // Delete restaurant
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        logger.info("Request received to delete restaurant with ID: {}", id);

        restaurantService.deleteRestaurant(id);
        logger.info("Restaurant deleted successfully with ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    // Check if a restaurant exists
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        logger.info("Request received to check if restaurant exists with ID: {}", id);
        boolean exists = restaurantService.existsById(id);
        logger.info("Restaurant with ID: {} exists: {}", id, exists);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    // Get a total count of restaurants
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalCount() {
        logger.info("Request received to get total count of restaurants");
        long count = restaurantService.getAllRestaurants().size();
        logger.info("Total restaurant count: {}", count);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}
