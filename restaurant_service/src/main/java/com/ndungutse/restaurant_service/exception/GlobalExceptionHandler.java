package com.ndungutse.restaurant_service.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

class ValidationErrorResponse {
    private Integer status;
    private String message;
    private Map<String, String> errors;
    private String path;
    private String timestamp;

    public ValidationErrorResponse() {

    }

    public String getMessage() {
        return message;
    }

    public Integer getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {

        logger.warn("Access denied: {}", ex.getMessage());
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "You are not allowed to perform this action. Please contact the administrator for further assistance.");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", "/api/v1/restaurants");

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {

        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "An unexpected error occurred in restaurantservice");
        errorResponse.put("path", "/api/v1/restaurants");

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        logger.error("Validation error occurred: {}", ex);

        Map<String, String> errors = new HashMap<>();

        ex.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), err.getDefaultMessage());
        });

        ValidationErrorResponse validationErrorResponse = new ValidationErrorResponse();
        validationErrorResponse.setStatus(400);
        validationErrorResponse.setMessage("Validation failed");
        validationErrorResponse.setTimestamp(LocalDateTime.now().toString());
        validationErrorResponse.setPath(request.getRequestURI());
        validationErrorResponse.setErrors(errors);

        return ResponseEntity.badRequest().body(validationErrorResponse);
    }
}
