package com.ndungutse.auth_service.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ndungutse.auth_service.model.Role;
import com.ndungutse.auth_service.model.User;
import com.ndungutse.auth_service.repository.RoleRepository;
import com.ndungutse.auth_service.repository.UserRepository;

// @Configuration
public class DatabaseSeeder {
    @Bean
    CommandLineRunner initDatabase(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Create roles if they don't exist
            // ROLE_CUSTOMER, ROLE_RESTAURANT_OWNER, ROLE_ADMIN
            List<Role> roles = Arrays.asList(
                    Role.builder().roleName("ADMIN").build(),
                    Role.builder().roleName("RESTAURANT_OWNER").build(),
                    Role.builder().roleName("CUSTOMER").build());

            roles.forEach(role -> {
                if (!roleRepository.existsByRoleName(role.getRoleName())) {
                    roleRepository.save(role);
                }
            });

            // Get roles
            Role adminRole = roleRepository.findByRoleName("ADMIN").orElseThrow();
            Role restaurantOwnerRole = roleRepository.findByRoleName("RESTAURANT_OWNER").orElseThrow();
            Role customerRole = roleRepository.findByRoleName("CUSTOMER").orElseThrow();

            // Create admin user
            if (!userRepository.existsByEmail("dav.ndungutse@gmail.com")) {
                User admin = User.builder()
                        .email("dav.ndungutse@gmail.com")
                        .password(passwordEncoder.encode("test123"))
                        .role(adminRole)
                        .fullName("Admin User")
                        .build();
                userRepository.save(admin);
            }

            // Create Restaurant owner user
            if (!userRepository.existsByEmail("owner@example.com")) {
                User manager = User.builder()
                        .email("owner@example.com")
                        .password(passwordEncoder.encode("test123"))
                        .role(restaurantOwnerRole)
                        .fullName("Restaurant Owner")
                        .build();
                userRepository.save(manager);
            }

            // Create CUSTOMER users
            String email = "customer@example.com";
            if (!userRepository.existsByEmail(email)) {
                User customer = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode("test123"))
                        .role(customerRole)
                        .fullName("Customer full name")
                        .build();
                userRepository.save(customer);
            }

        };
    }
}
