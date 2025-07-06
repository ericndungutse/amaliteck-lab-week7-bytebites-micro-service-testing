package com.ndungutse.restaurant_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ndungutse.restaurant_service.model.Restaurant;
import com.ndungutse.restaurant_service.repository.RestaurantRepository;

// @Configuration
public class Seed {

    @Bean
    CommandLineRunner initDatabase(RestaurantRepository restaurantRepository) {
        return args -> {
            // Seed initial data
            Restaurant restaurant1 = new Restaurant();
            restaurant1.setName("Restaurant 1");
            restaurant1.setOwner(2L);

            restaurantRepository.save(restaurant1);

        };
    }

}
