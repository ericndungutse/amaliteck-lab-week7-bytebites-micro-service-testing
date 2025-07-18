package com.ndungutse.auth_service.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        System.out.println("************************** " + request.getRequestURI());

        Map<String, String> error = new HashMap<>();
        error.put("message", authException.getMessage());
        error.put("status", String.valueOf(HttpStatus.UNAUTHORIZED.value()));
        error.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        error.put("path", request.getRequestURI());
        response.setContentType("application/json");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        objectMapper.writeValue(response.getWriter(), error);
    }

}
