package com.ndungutse.api_gateway;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallBackController {
    @RequestMapping("/restaurant-fallback")
    public ResponseEntity<Map<String, String>> restaurantFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Restaurant service is temporarily unavailable. Please try again later.");
        response.put("status", "503 Service Unavailable");
        response.put("error", "Service Unavailable");
        response.put("path", "/restaurant");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
