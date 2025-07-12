package com.ndungutse.restaurant_service.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndungutse.restaurant_service.dto.RestaurantRequestDto;
import com.ndungutse.restaurant_service.model.Restaurant;
import com.ndungutse.restaurant_service.repository.RestaurantRepository;
import com.ndungutse.restaurant_service.security.UserPrincipal;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class RestaurantControllerIntegrationTest {

        @Container
        private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:14-alpine")
                        .withDatabaseName("restaurant_test")
                        .withUsername("test")
                        .withPassword("test");

        @DynamicPropertySource
        static void postgresqlProperties(DynamicPropertyRegistry registry) {
                registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
                registry.add("spring.datasource.username", postgresContainer::getUsername);
                registry.add("spring.datasource.password", postgresContainer::getPassword);
                registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
                // Disable Eureka client for tests
                registry.add("eureka.client.enabled", () -> "false");
                registry.add("spring.cloud.config.enabled", () -> "false");
        }

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private RestaurantRepository restaurantRepository;

        @Autowired
        private ObjectMapper objectMapper;

        private final Long userId = 1L;
        private final Long otherUserId = 2L;
        private Restaurant testRestaurant;

        @BeforeEach
        void setUp() {
                // Clean up database
                restaurantRepository.deleteAll();

                // Create test restaurant
                testRestaurant = new Restaurant("Test Restaurant", userId);
                testRestaurant = restaurantRepository.save(testRestaurant);

                // Set up security context with ADMIN role
                UserPrincipal userPrincipal = new UserPrincipal(
                                userId.toString(),
                                "admin@example.com",
                                "Admin User",
                                "ROLE_ADMIN");

                var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
                var authentication = new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @AfterEach
        void tearDown() {
                // Clear security context
                SecurityContextHolder.clearContext();
        }

        @Test
        void getAllRestaurants_ReturnsAllRestaurants() throws Exception {
                // Act & Assert
                mockMvc.perform(get("/api/v1/restaurants"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].id", is(testRestaurant.getId().intValue())))
                                .andExpect(jsonPath("$[0].name", is("Test Restaurant")))
                                .andExpect(jsonPath("$[0].owner", is(userId.intValue())));
        }

        @Test
        void getRestaurantById_ExistingId_ReturnsRestaurant() throws Exception {
                // Act & Assert
                mockMvc.perform(get("/api/v1/restaurants/{id}", testRestaurant.getId()))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.id", is(testRestaurant.getId().intValue())))
                                .andExpect(jsonPath("$.name", is("Test Restaurant")))
                                .andExpect(jsonPath("$.owner", is(userId.intValue())));
        }

        @Test
        void getRestaurantById_NonExistingId_ReturnsNotFound() throws Exception {
                // Act & Assert
                mockMvc.perform(get("/api/v1/restaurants/{id}", 999L))
                                .andExpect(status().isNotFound());
        }

        @Test
        void createRestaurant_ValidInput_ReturnsCreatedRestaurant() throws Exception {
                // Arrange
                RestaurantRequestDto requestDto = new RestaurantRequestDto("New Restaurant", userId);
                String requestJson = objectMapper.writeValueAsString(requestDto);

                // Act & Assert
                mockMvc.perform(post("/api/v1/restaurants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.name", is("New Restaurant")))
                                .andExpect(jsonPath("$.owner", is(userId.intValue())));

                // Verify restaurant was created in database
                List<Restaurant> restaurants = restaurantRepository.findAll();
                assertEquals(2, restaurants.size());
                assertTrue(restaurants.stream().anyMatch(r -> r.getName().equals("New Restaurant")));
        }

        @Test
        void updateRestaurant_ValidInput_ReturnsUpdatedRestaurant() throws Exception {
                // Arrange
                RestaurantRequestDto requestDto = new RestaurantRequestDto("Updated Restaurant", userId);
                String requestJson = objectMapper.writeValueAsString(requestDto);

                // Act & Assert
                mockMvc.perform(put("/api/v1/restaurants/{id}", testRestaurant.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.id", is(testRestaurant.getId().intValue())))
                                .andExpect(jsonPath("$.name", is("Updated Restaurant")))
                                .andExpect(jsonPath("$.owner", is(userId.intValue())));

                // Verify restaurant was updated in database
                Restaurant updatedRestaurant = restaurantRepository.findById(testRestaurant.getId()).orElseThrow();
                assertEquals("Updated Restaurant", updatedRestaurant.getName());
        }

        @Test
        void deleteRestaurant_ExistingId_ReturnsNoContent() throws Exception {
                // Act & Assert
                mockMvc.perform(delete("/api/v1/restaurants/{id}", testRestaurant.getId()))
                                .andExpect(status().isNoContent());

                // Verify restaurant was deleted from database
                assertFalse(restaurantRepository.existsById(testRestaurant.getId()));
        }

        @Test
        void getMyRestaurants_AsRestaurantOwner_ReturnsOwnedRestaurants() throws Exception {
                // Arrange
                // Create another restaurant owned by a different user
                Restaurant otherRestaurant = new Restaurant("Other Restaurant", otherUserId);
                restaurantRepository.save(otherRestaurant);

                // Set up security context with RESTAURANT_OWNER role
                UserPrincipal userPrincipal = new UserPrincipal(
                                userId.toString(),
                                "owner@example.com",
                                "Restaurant Owner",
                                "ROLE_RESTAURANT_OWNER");

                var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER"));
                var authentication = new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Act & Assert
                mockMvc.perform(get("/api/v1/restaurants/my-restaurants"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].id", is(testRestaurant.getId().intValue())))
                                .andExpect(jsonPath("$[0].name", is("Test Restaurant")))
                                .andExpect(jsonPath("$[0].owner", is(userId.intValue())));
        }

        @Test
        void getMyRestaurants_WithoutRequiredRole_ReturnsForbidden() throws Exception {
                // Arrange - Set up security context without RESTAURANT_OWNER role
                UserPrincipal userPrincipal = new UserPrincipal(
                                userId.toString(),
                                "user@example.com",
                                "Regular User",
                                "ROLE_USER");

                var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
                var authentication = new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Act & Assert
                mockMvc.perform(get("/api/v1/restaurants/my-restaurants"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void createRestaurant_WithoutAdminRole_ReturnsForbidden() throws Exception {
                // Arrange
                RestaurantRequestDto requestDto = new RestaurantRequestDto("New Restaurant", userId);
                String requestJson = objectMapper.writeValueAsString(requestDto);

                // Set up security context without ADMIN role
                UserPrincipal userPrincipal = new UserPrincipal(
                                userId.toString(),
                                "user@example.com",
                                "Regular User",
                                "ROLE_USER");

                var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
                var authentication = new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Act & Assert
                mockMvc.perform(post("/api/v1/restaurants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isForbidden());
        }

        @Test
        void updateRestaurant_NotOwner_ReturnsAccessDenied() throws Exception {
                // Arrange
                // Create a restaurant owned by another user
                Restaurant otherRestaurant = new Restaurant("Other Restaurant", otherUserId);
                otherRestaurant = restaurantRepository.save(otherRestaurant);

                RestaurantRequestDto requestDto = new RestaurantRequestDto("Updated Restaurant", userId);
                String requestJson = objectMapper.writeValueAsString(requestDto);

                // Act & Assert
                mockMvc.perform(put("/api/v1/restaurants/{id}", otherRestaurant.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isForbidden());
        }

        @Test
        void deleteRestaurant_NotOwner_ReturnsAccessDenied() throws Exception {
                // Arrange
                // Create a restaurant owned by another user
                Restaurant otherRestaurant = new Restaurant("Other Restaurant", otherUserId);
                otherRestaurant = restaurantRepository.save(otherRestaurant);

                // Act & Assert
                mockMvc.perform(delete("/api/v1/restaurants/{id}", otherRestaurant.getId()))
                                .andExpect(status().isForbidden());
        }
}