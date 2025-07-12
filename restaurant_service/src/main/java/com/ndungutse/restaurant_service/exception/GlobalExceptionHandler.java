package com.ndungutse.restaurant_service.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Setter
@Getter
class ValidationErrorResponse {
    private Integer status;
    private String message;
    private Map<String, String> errors;
    private String path;
    private String timestamp;

    public ValidationErrorResponse() {

    }

}
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRestaurantNotFoundException(RestaurantNotFoundException ex) {
        logger.error("Restaurant not found: {}", ex.getMessage());
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", "/api/v1/restaurants");
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {

        logger.error("Access denied: {}", ex.getMessage());
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        logger.error("Method argument type mismatch: {}", ex.getMessage());
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("message", "Invalid argument type");
        errorResponse.put("path", "/api/v1/restaurants");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
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
