package com.ndungutse.auth_service.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ndungutse.auth_service.dto.LoginRequest;
import com.ndungutse.auth_service.dto.LoginResponse;
import com.ndungutse.auth_service.repository.RoleRepository;
import com.ndungutse.auth_service.repository.UserRepository;
import com.ndungutse.auth_service.security.JwtUtils;
import org.springframework.security.core.Authentication;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
            UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        // Authenticate user with username or email
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        // Get authenticated user details
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Generate JWT token
        String token = jwtUtils.generateJwtTokenFromUsername(userDetails);

        // Build and return response
        return LoginResponse.builder()
                .token(token)
                .userId(userDetails.getUserId())
                .email(userDetails.getUser().getEmail())
                .role(userDetails.getRoleName())
                .build();
    }

}
