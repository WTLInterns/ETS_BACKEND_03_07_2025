package com.example.demo.Controller;


import com.example.demo.Service.Location.LocationValidationResult;
import com.example.demo.Service.Location.LocationValidationService;
import com.example.demo.Service.ScheduleBookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/location")
public class LocationValidationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationValidationController.class);

    @Autowired
    private LocationValidationService locationValidationService;

    @Autowired
    private ScheduleBookingService scheduleBookingService;

    /**
     * Test location validation endpoint
     */
    @GetMapping("/validate")
    public ResponseEntity<LocationValidationResult> validateLocation(
            @RequestParam String location,
            @RequestParam(defaultValue = "test") String type) {

        try {
            LocationValidationResult result = locationValidationService.validateLocation(location, type);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error validating location: {}", e.getMessage());
            LocationValidationResult error = LocationValidationResult.error(
                    "VALIDATION_ERROR",
                    "Error during validation: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Validate both pickup and drop locations
     */
    @PostMapping("/validate-trip")
    public ResponseEntity<LocationValidationResult> validateTrip(
            @RequestParam String pickupLocation,
            @RequestParam String dropLocation) {

        try {
            LocationValidationResult result = scheduleBookingService.validatePickupAndDropLocations(
                    pickupLocation, dropLocation
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error validating trip: {}", e.getMessage());
            LocationValidationResult error = LocationValidationResult.error(
                    "TRIP_VALIDATION_ERROR",
                    "Error during trip validation: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get coordinates for a location
     */
    @GetMapping("/coordinates")
    public ResponseEntity<Map<String, Object>> getCoordinates(@RequestParam String location) {
        Map<String, Object> response = new HashMap<>();

        try {
            double[] coordinates = locationValidationService.getCoordinatesWithCache(location);

            if (coordinates != null) {
                response.put("success", true);
                response.put("location", location);
                response.put("latitude", coordinates[0]);
                response.put("longitude", coordinates[1]);

                // Check if within Pune bounds
                LocationValidationResult validation = locationValidationService.validateLocation(location, "test");
                response.put("isValidPuneLocation", validation.isValid());
                response.put("validationMethod", validation.getValidationMethod());

            } else {
                response.put("success", false);
                response.put("message", "Could not get coordinates for location");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting coordinates: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = scheduleBookingService.getLocationServiceHealth();

            if ("UP".equals(health.get("status"))) {
                return ResponseEntity.ok(health);
            } else {
                return ResponseEntity.status(503).body(health);
            }

        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage());
            Map<String, Object> errorHealth = new HashMap<>();
            errorHealth.put("status", "DOWN");
            errorHealth.put("error", e.getMessage());
            return ResponseEntity.status(503).body(errorHealth);
        }
    }

    /**
     * Cache management endpoint
     */
    @PostMapping("/admin/clear-cache")
    public ResponseEntity<Map<String, String>> clearCache() {
        try {
            scheduleBookingService.clearLocationCache();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Location validation cache cleared successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error clearing cache: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to clear cache: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Cache statistics endpoint
     */
    @GetMapping("/admin/cache-stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        try {
            Map<String, Object> stats = locationValidationService.getCacheStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting cache stats: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Test endpoint specifically for debugging the Kharadi issue
     */
    @GetMapping("/debug/kharadi")
    public ResponseEntity<Map<String, Object>> debugKharadi() {
        Map<String, Object> debug = new HashMap<>();

        String[] testLocations = {
                "Kharadi, Pune",
                "5A, Suit 941, 4th Floor, City Vista, Tower A, Kolte-Patil Downtown, Ashoka Nagar, Kharadi, Pune, Maharashtra 411014, India",
                "Kharadi",
                "City Vista, Kharadi",
                "Ashoka Nagar, Kharadi, Pune"
        };

        for (String location : testLocations) {
            try {
                Map<String, Object> locationResult = new HashMap<>();

                // Test validation
                LocationValidationResult validation = locationValidationService.validateLocation(location, "debug");
                locationResult.put("isValid", validation.isValid());
                locationResult.put("validationMethod", validation.getValidationMethod());
                locationResult.put("errorCode", validation.getErrorCode());
                locationResult.put("errorMessage", validation.getErrorMessage());

                // Test coordinates
                double[] coords = locationValidationService.getCoordinatesWithCache(location);
                if (coords != null) {
                    locationResult.put("latitude", coords[0]);
                    locationResult.put("longitude", coords[1]);
                } else {
                    locationResult.put("coordinates", "null");
                }

                debug.put(location, locationResult);

            } catch (Exception e) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", e.getMessage());
                debug.put(location, errorResult);
            }
        }

        return ResponseEntity.ok(debug);
    }
}