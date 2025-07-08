package com.ndungutse.order_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ndungutse.order_service.exception.CustomAuthEntryPoint;
import com.ndungutse.order_service.security.HeaderAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConf {
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final HeaderAuthenticationFilter headerAuthenticationFilter;

    public SecurityConf(CustomAuthEntryPoint customAuthEntryPoint,
            HeaderAuthenticationFilter headerAuthenticationFilter) {
        this.customAuthEntryPoint = customAuthEntryPoint;
        this.headerAuthenticationFilter = headerAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection as this is a microservice (stateless API)
                .csrf(AbstractHttpConfigurer::disable)

                // Disable form login
                .formLogin(AbstractHttpConfigurer::disable)

                // Set session creation policy to stateless (no sessions)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure authorization rules
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders/resilience-checker").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated())
                .httpBasic(AbstractHttpConfigurer::disable)

                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthEntryPoint));

        return http.build();
    }

}
