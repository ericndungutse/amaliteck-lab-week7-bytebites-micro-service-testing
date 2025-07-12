package com.ndungutse.restaurant_service.service;

import com.ndungutse.restaurant_service.dto.RestaurantDto;
import com.ndungutse.restaurant_service.dto.RestaurantRequestDto;
import com.ndungutse.restaurant_service.exception.RestaurantNotFoundException;
import com.ndungutse.restaurant_service.mapper.RestaurantMapper;
import com.ndungutse.restaurant_service.model.Restaurant;
import com.ndungutse.restaurant_service.repository.RestaurantRepository;
import com.ndungutse.restaurant_service.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;

    public RestaurantDto createRestaurant(RestaurantRequestDto requestDto) {
        logger.info("Creating new restaurant with name: {}", requestDto.getName());

        // Set the owner from the authenticated user
        UserPrincipal currentUser = getCurrentUser();
        Long ownerId = Long.parseLong(currentUser.getUserId());
        requestDto.setOwner(ownerId);

        logger.debug("Setting owner ID: {} for restaurant: {}", ownerId, requestDto.getName());

        Restaurant restaurant = restaurantMapper.toEntity(requestDto);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        logger.info("Restaurant created successfully with ID: {}, owner: {}",
                savedRestaurant.getId(), savedRestaurant.getOwner());

        return restaurantMapper.toDto(savedRestaurant);
    }

    public List<RestaurantDto> getAllRestaurants() {
        logger.info("Fetching all restaurants");
        List<Restaurant> restaurants = restaurantRepository.findAll();
        logger.debug("Found {} restaurants", restaurants.size());
        return restaurantMapper.toDtoList(restaurants);
    }

    public Optional<RestaurantDto> getRestaurantById(Long id) {
        logger.info("Fetching restaurant with ID: {}", id);
        Optional<Restaurant> restaurant = restaurantRepository.findById(id);
        if (restaurant.isPresent()) {
            logger.debug("Restaurant found with ID: {}", id);
        } else {
            logger.debug("Restaurant not found with ID: {}", id);
        }
        return restaurant.map(restaurantMapper::toDto);
    }

    public RestaurantDto updateRestaurant(Long id, RestaurantRequestDto requestDto) {
        logger.info("Updating restaurant with ID: {}", id);

        return restaurantRepository.findById(id)
                .map(restaurant -> {
                    // Validate ownership
                    UserPrincipal currentUser = getCurrentUser();
                    Long userId = Long.parseLong(currentUser.getUserId());
                    logger.debug("User {} attempting to update restaurant {}", userId, id);

                    validateOwnership(restaurant);

                    restaurantMapper.updateEntityFromRequestDto(restaurant, requestDto);
                    Restaurant updatedRestaurant = restaurantRepository.save(restaurant);

                    logger.info("Restaurant updated successfully with ID: {}, owner: {}",
                            updatedRestaurant.getId(), updatedRestaurant.getOwner());

                    return restaurantMapper.toDto(updatedRestaurant);
                })
                .orElseThrow(() -> {
                    logger.error("Restaurant not found with ID: {}", id);
                    return new RestaurantNotFoundException("Restaurant not found with id: " + id);
                });
    }

    public void deleteRestaurant(Long id) {
        logger.info("Deleting restaurant with ID: {}", id);

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> {
                    logger.info("Restaurant not found with ID: {}", id);
                    return new RestaurantNotFoundException("Restaurant not found with id: " + id);
                });

        // Validate ownership
        UserPrincipal currentUser = getCurrentUser();
        Long userId = Long.parseLong(currentUser.getUserId());
        logger.debug("User {} attempting to delete restaurant {}", userId, id);

        validateOwnership(restaurant);
        logger.debug("Ownership validated for restaurant ID: {}, owner: {}",
                id, restaurant.getOwner());

        restaurantRepository.deleteById(id);
        logger.info("Restaurant deleted successfully with ID: {}", id);
    }

    public boolean existsById(Long id) {
        logger.debug("Checking if restaurant exists with ID: {}", id);
        boolean exists = restaurantRepository.existsById(id);
        logger.debug("Restaurant with ID: {} exists: {}", id, exists);
        return exists;
    }

    public List<RestaurantDto> getMyRestaurants() {
        logger.info("Fetching restaurants for current user");

        UserPrincipal currentUser = getCurrentUser();
        Long ownerId = Long.parseLong(currentUser.getUserId());
        logger.debug("Fetching restaurants for user ID: {}", ownerId);

        List<Restaurant> restaurants = restaurantRepository.findAll()
                .stream()
                .filter(restaurant -> restaurant.getOwner().equals(ownerId))
                .toList();

        logger.debug("Found {} restaurants for user ID: {}", restaurants.size(), ownerId);
        return restaurantMapper.toDtoList(restaurants);
    }

    // Helper method to get a current authenticated user
    private UserPrincipal getCurrentUser() {
        logger.debug("Getting current authenticated user");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            logger.error("Authentication failed: No valid authentication found");
            throw new AccessDeniedException("Authentication required.");
        }
        logger.debug("Current user retrieved: ID={}, role={}",
                userPrincipal.getUserId(), userPrincipal.getRole());
        return userPrincipal;
    }

    // Helper method to validate ownership of a restaurant
    private void validateOwnership(Restaurant restaurant) {
        UserPrincipal currentUser = getCurrentUser();
        Long currentUserId = Long.parseLong(currentUser.getUserId());
        Long restaurantOwnerId = restaurant.getOwner();

        logger.debug("Validating ownership: User ID={}, Restaurant ID={}, Owner ID={}",
                currentUserId, restaurant.getId(), restaurantOwnerId);

        if (!restaurantOwnerId.equals(currentUserId)) {
            logger.warn("Access denied: User {} attempted to access restaurant {} owned by {}",
                    currentUserId, restaurant.getId(), restaurantOwnerId);
            throw new AccessDeniedException("Access denied. You can only manage your own restaurants.");
        }

        logger.debug("Ownership validated successfully for restaurant ID: {}", restaurant.getId());
    }
}
