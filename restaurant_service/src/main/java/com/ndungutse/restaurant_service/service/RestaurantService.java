package com.ndungutse.restaurant_service.service;

import com.ndungutse.restaurant_service.dto.RestaurantDto;
import com.ndungutse.restaurant_service.dto.RestaurantRequestDto;
import com.ndungutse.restaurant_service.mapper.RestaurantMapper;
import com.ndungutse.restaurant_service.model.Restaurant;
import com.ndungutse.restaurant_service.repository.RestaurantRepository;
import com.ndungutse.restaurant_service.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;

    public RestaurantDto createRestaurant(RestaurantRequestDto requestDto) {

        // Set the owner from the authenticated user
        UserPrincipal currentUser = getCurrentUser();
        requestDto.setOwner(Long.parseLong(currentUser.getUserId()));

        Restaurant restaurant = restaurantMapper.toEntity(requestDto);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return restaurantMapper.toDto(savedRestaurant);
    }

    public List<RestaurantDto> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        return restaurantMapper.toDtoList(restaurants);
    }

    public Optional<RestaurantDto> getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .map(restaurantMapper::toDto);
    }

    public RestaurantDto updateRestaurant(Long id, RestaurantRequestDto requestDto) {

        return restaurantRepository.findById(id)
                .map(restaurant -> {
                    // Validate ownership
                    validateOwnership(restaurant);

                    restaurantMapper.updateEntityFromRequestDto(restaurant, requestDto);
                    Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
                    return restaurantMapper.toDto(updatedRestaurant);
                })
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
    }

    public void deleteRestaurant(Long id) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));

        // Validate ownership
        validateOwnership(restaurant);

        restaurantRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return restaurantRepository.existsById(id);
    }

    public List<RestaurantDto> getMyRestaurants() {
        UserPrincipal currentUser = getCurrentUser();
        Long ownerId = Long.parseLong(currentUser.getUserId());

        List<Restaurant> restaurants = restaurantRepository.findAll()
                .stream()
                .filter(restaurant -> restaurant.getOwner().equals(ownerId))
                .toList();
        return restaurantMapper.toDtoList(restaurants);
    }

    // Helper method to get current authenticated user
    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new AccessDeniedException("Authentication required.");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    // Helper method to validate ownership of a restaurant
    private void validateOwnership(Restaurant restaurant) {
        UserPrincipal currentUser = getCurrentUser();
        Long currentUserId = Long.parseLong(currentUser.getUserId());

        if (!restaurant.getOwner().equals(currentUserId)) {
            throw new AccessDeniedException("Access denied. You can only manage your own restaurants.");
        }
    }
}
