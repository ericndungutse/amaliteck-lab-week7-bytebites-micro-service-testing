package com.ndungutse.restaurant_service.service;

import com.ndungutse.restaurant_service.dto.RestaurantDto;
import com.ndungutse.restaurant_service.dto.RestaurantRequestDto;
import com.ndungutse.restaurant_service.exception.RestaurantNotFoundException;
import com.ndungutse.restaurant_service.mapper.RestaurantMapper;
import com.ndungutse.restaurant_service.model.Restaurant;
import com.ndungutse.restaurant_service.repository.RestaurantRepository;
import com.ndungutse.restaurant_service.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantMapper restaurantMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RestaurantService restaurantService;

    private UserPrincipal userPrincipal;
    private Restaurant restaurant;
    private RestaurantDto restaurantDto;
    private RestaurantRequestDto requestDto;
    private final Long userId = 1L;
    private final Long restaurantId = 1L;

    @BeforeEach
    void setUp() {
        // Set up security context with lenient stubbing
        userPrincipal = new UserPrincipal(userId.toString(), "user@example.com", "Test User", "ROLE_USER");
        Mockito.lenient().when(authentication.getPrincipal()).thenReturn(userPrincipal);
        Mockito.lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Set up test data
        restaurant = new Restaurant("Test Restaurant", userId);
        restaurant.setId(restaurantId);

        restaurantDto = new RestaurantDto(restaurantId, "Test Restaurant", userId);

        requestDto = new RestaurantRequestDto("Test Restaurant", userId);
    }

    @Test
    void createRestaurant_Success() {
        // Arrange
        when(restaurantMapper.toEntity(any(RestaurantRequestDto.class))).thenReturn(restaurant);
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);
        when(restaurantMapper.toDto(any(Restaurant.class))).thenReturn(restaurantDto);

        // Act
        RestaurantDto result = restaurantService.createRestaurant(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(restaurantId, result.getId());
        assertEquals("Test Restaurant", result.getName());
        assertEquals(userId, result.getOwner());

        // Verify
        verify(restaurantMapper).toEntity(any(RestaurantRequestDto.class));
        verify(restaurantRepository).save(any(Restaurant.class));
        verify(restaurantMapper).toDto(any(Restaurant.class));
    }

    @Test
    void getAllRestaurants_Success() {
        // Arrange
        List<Restaurant> restaurants = Arrays.asList(restaurant);
        List<RestaurantDto> restaurantDtos = Arrays.asList(restaurantDto);

        when(restaurantRepository.findAll()).thenReturn(restaurants);
        when(restaurantMapper.toDtoList(restaurants)).thenReturn(restaurantDtos);

        // Act
        List<RestaurantDto> result = restaurantService.getAllRestaurants();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(restaurantId, result.get(0).getId());

        // Verify
        verify(restaurantRepository).findAll();
        verify(restaurantMapper).toDtoList(restaurants);
    }

    @Test
    void getRestaurantById_Success() {
        // Arrange
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantMapper.toDto(restaurant)).thenReturn(restaurantDto);

        // Act
        Optional<RestaurantDto> result = restaurantService.getRestaurantById(restaurantId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(restaurantId, result.get().getId());
        assertEquals("Test Restaurant", result.get().getName());

        // Verify
        verify(restaurantRepository).findById(restaurantId);
        verify(restaurantMapper).toDto(restaurant);
    }

    @Test
    void getRestaurantById_NotFound() {
        // Arrange
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // Act
        Optional<RestaurantDto> result = restaurantService.getRestaurantById(restaurantId);

        // Assert
        assertFalse(result.isPresent());

        // Verify
        verify(restaurantRepository).findById(restaurantId);
        verify(restaurantMapper, never()).toDto(any(Restaurant.class));
    }

    @Test
    void updateRestaurant_Success() {
        // Arrange
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);
        when(restaurantMapper.toDto(restaurant)).thenReturn(restaurantDto);

        // Act
        RestaurantDto result = restaurantService.updateRestaurant(restaurantId, requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(restaurantId, result.getId());
        assertEquals("Test Restaurant", result.getName());

        // Verify
        verify(restaurantRepository).findById(restaurantId);
        verify(restaurantMapper).updateEntityFromRequestDto(restaurant, requestDto);
        verify(restaurantRepository).save(restaurant);
        verify(restaurantMapper).toDto(restaurant);
    }

    @Test
    void updateRestaurant_NotFound() {
        // Arrange
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RestaurantNotFoundException.class, () -> {
            restaurantService.updateRestaurant(restaurantId, requestDto);
        });

        // Verify
        verify(restaurantRepository).findById(restaurantId);
        verify(restaurantMapper, never()).updateEntityFromRequestDto(any(), any());
        verify(restaurantRepository, never()).save(any());
    }

    @Test
    void updateRestaurant_AccessDenied() {
        // Arrange
        Restaurant otherRestaurant = new Restaurant("Other Restaurant", 2L);
        otherRestaurant.setId(restaurantId);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(otherRestaurant));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            restaurantService.updateRestaurant(restaurantId, requestDto);
        });

        // Verify
        verify(restaurantRepository).findById(restaurantId);
        verify(restaurantMapper, never()).updateEntityFromRequestDto(any(), any());
        verify(restaurantRepository, never()).save(any());
    }

    @Test
    void deleteRestaurant_Success() {
        // Arrange
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        // Act
        restaurantService.deleteRestaurant(restaurantId);

        // Verify
        verify(restaurantRepository).findById(restaurantId);
        verify(restaurantRepository).deleteById(restaurantId);
    }

    @Test
    void deleteRestaurant_NotFound() {
        // Arrange
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RestaurantNotFoundException.class, () -> {
            restaurantService.deleteRestaurant(restaurantId);
        });

        // Verify
        verify(restaurantRepository).findById(restaurantId);
        verify(restaurantRepository, never()).deleteById(any());
    }

    @Test
    void deleteRestaurant_AccessDenied() {
        // Arrange
        Restaurant otherRestaurant = new Restaurant("Other Restaurant", 2L);
        otherRestaurant.setId(restaurantId);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(otherRestaurant));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            restaurantService.deleteRestaurant(restaurantId);
        });

        // Verify
        verify(restaurantRepository).findById(restaurantId);
        verify(restaurantRepository, never()).deleteById(any());
    }

    @Test
    void existsById_True() {
        // Arrange
        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);

        // Act
        boolean result = restaurantService.existsById(restaurantId);

        // Assert
        assertTrue(result);

        // Verify
        verify(restaurantRepository).existsById(restaurantId);
    }

    @Test
    void existsById_False() {
        // Arrange
        when(restaurantRepository.existsById(restaurantId)).thenReturn(false);

        // Act
        boolean result = restaurantService.existsById(restaurantId);

        // Assert
        assertFalse(result);

        // Verify
        verify(restaurantRepository).existsById(restaurantId);
    }

    @Test
    void getMyRestaurants_Success() {
        // Arrange
        List<Restaurant> restaurants = Arrays.asList(restaurant);
        List<RestaurantDto> restaurantDtos = Arrays.asList(restaurantDto);

        when(restaurantRepository.findAll()).thenReturn(restaurants);
        when(restaurantMapper.toDtoList(restaurants)).thenReturn(restaurantDtos);

        // Act
        List<RestaurantDto> result = restaurantService.getMyRestaurants();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(restaurantId, result.get(0).getId());

        // Verify
        verify(restaurantRepository).findAll();
        verify(restaurantMapper).toDtoList(restaurants);
    }

    @Test
    void getMyRestaurants_NoRestaurants() {
        // Arrange
        Restaurant otherRestaurant = new Restaurant("Other Restaurant", 2L);
        otherRestaurant.setId(2L);
        List<Restaurant> restaurants = Arrays.asList(otherRestaurant);

        when(restaurantRepository.findAll()).thenReturn(restaurants);
        when(restaurantMapper.toDtoList(any())).thenReturn(Arrays.asList());

        // Act
        List<RestaurantDto> result = restaurantService.getMyRestaurants();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verify(restaurantRepository).findAll();
    }
}
