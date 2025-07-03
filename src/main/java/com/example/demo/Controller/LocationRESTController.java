package com.example.demo.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.DTO.LocationMessage;
import com.example.demo.Utils.LocationUtils;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/location")
@CrossOrigin("*") // For testing purposes
public class LocationRESTController {

    // Store user locations by slot ID and user ID
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, LocationMessage>> userLocationsBySlot = new ConcurrentHashMap<>();
    
    // Store driver locations by slot ID
    private static final ConcurrentHashMap<String, LocationMessage> driverLocationsBySlot = new ConcurrentHashMap<>();
    
    // Store user details by slot ID
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, LocationMessage>> userDetailsBySlot = new ConcurrentHashMap<>();
    
    // Store OTPs by userId
    private static final ConcurrentHashMap<String, String> otpsByUserId = new ConcurrentHashMap<>();

    @PostMapping("/user/update")
    public ResponseEntity<Map<String, Object>> updateUserLocation(@RequestBody LocationMessage locationMessage) {
        String slotId = locationMessage.getSlotId();
        String userId = locationMessage.getUserId();
        
        // Set the user's current location coordinates
        double userLat = locationMessage.getLatitude();
        double userLng = locationMessage.getLongitude();
        
        // If pickup coordinates are not set, use current location as pickup by default
        if (locationMessage.getPickupLatitude() == 0 && locationMessage.getPickupLongitude() == 0) {
            locationMessage.setPickupLatitude(userLat);
            locationMessage.setPickupLongitude(userLng);
        }
        
        // If there's a driver for this slot, calculate distance and ETA
        if (driverLocationsBySlot.containsKey(slotId)) {
            LocationMessage driverLocation = driverLocationsBySlot.get(slotId);
            double driverLat = driverLocation.getLatitude();
            double driverLng = driverLocation.getLongitude();
            
            // Calculate distance from driver to user's pickup location
            double distanceToPickup = LocationUtils.calculateDistance(
                driverLat, driverLng, 
                locationMessage.getPickupLatitude(), locationMessage.getPickupLongitude()
            );
            
            // Calculate ETA
            int estimatedTimeToPickup = LocationUtils.estimateTravelTime(distanceToPickup);
            
            // Calculate total ride distance
            double totalRideDistance = LocationUtils.calculateDistance(
                locationMessage.getPickupLatitude(), locationMessage.getPickupLongitude(),
                locationMessage.getDropLatitude(), locationMessage.getDropLongitude()
            );
            
            int estimatedRideTime = LocationUtils.estimateTravelTime(totalRideDistance);
            
            // Update the location message with calculated values
            locationMessage.setDistanceToPickup(distanceToPickup);
            locationMessage.setEstimatedTimeToPickup(estimatedTimeToPickup);
            locationMessage.setTotalRideDistance(totalRideDistance);
            locationMessage.setEstimatedRideTime(estimatedRideTime);
        }
        
        // Set last updated timestamp
        locationMessage.setLastUpdated(LocalDateTime.now());
        
        // Store user location
        if (!userLocationsBySlot.containsKey(slotId)) {
            userLocationsBySlot.put(slotId, new ConcurrentHashMap<>());
        }
        userLocationsBySlot.get(slotId).put(userId, locationMessage);
        
        // Store user details for pickup/drop info
        if (!userDetailsBySlot.containsKey(slotId)) {
            userDetailsBySlot.put(slotId, new ConcurrentHashMap<>());
        }
        userDetailsBySlot.get(slotId).put(userId, locationMessage);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        
        // Return driver location if available
        if (driverLocationsBySlot.containsKey(slotId)) {
            LocationMessage driverLocation = driverLocationsBySlot.get(slotId);
            response.put("driverLocation", driverLocation);
            response.put("distanceToPickup", locationMessage.getDistanceToPickup());
            response.put("estimatedTimeToPickup", locationMessage.getEstimatedTimeToPickup());
            response.put("totalRideDistance", locationMessage.getTotalRideDistance());
            response.put("estimatedRideTime", locationMessage.getEstimatedRideTime());
            response.put("rideStatus", driverLocation.getRideStatus());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/driver/update")
    public ResponseEntity<Map<String, Object>> updateDriverLocation(@RequestBody LocationMessage locationMessage) {
        String slotId = locationMessage.getSlotId();
        double driverLat = locationMessage.getLatitude();
        double driverLng = locationMessage.getLongitude();
        String currentRideStatus = locationMessage.getRideStatus(); // Get current ride status from driver

        // Store driver location
        locationMessage.setLastUpdated(LocalDateTime.now());
        driverLocationsBySlot.put(slotId, locationMessage);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");

        // Process each user in this slot to update distances and potentially add to response
        if (userLocationsBySlot.containsKey(slotId)) {
            ConcurrentHashMap<String, LocationMessage> users = userLocationsBySlot.get(slotId);
            boolean firstUserProcessed = false;

            for (String userId : users.keySet()) {
                LocationMessage userLocation = users.get(userId);

                // Calculate distance from driver to user's pickup location
                double distanceToPickup = LocationUtils.calculateDistance(
                    driverLat, driverLng,
                    userLocation.getPickupLatitude(), userLocation.getPickupLongitude()
                );

                // Calculate ETA to pickup
                int estimatedTimeToPickup = LocationUtils.estimateTravelTime(distanceToPickup);

                // Calculate total ride distance (pickup to drop for this user)
                double totalRideDistance = LocationUtils.calculateDistance(
                    userLocation.getPickupLatitude(), userLocation.getPickupLongitude(),
                    userLocation.getDropLatitude(), userLocation.getDropLongitude()
                );
                int estimatedRideTime = LocationUtils.estimateTravelTime(totalRideDistance);

                // Update the user's location message with these details
                userLocation.setDistanceToPickup(distanceToPickup);
                userLocation.setEstimatedTimeToPickup(estimatedTimeToPickup);
                userLocation.setTotalRideDistance(totalRideDistance);
                userLocation.setEstimatedRideTime(estimatedRideTime);
                // userLocation.setRideStatus(currentRideStatus); // User's ride status is updated via /updateStatus
                userLocation.setLastUpdated(LocalDateTime.now());

                users.put(userId, userLocation);

                // For the main response, send back details for the first user found (or selected user if implemented)
                // This is a simplification; ideally, the frontend would specify which user's details it needs.
                if (!firstUserProcessed) {
                    response.put("distanceToPickup", distanceToPickup);
                    response.put("estimatedTimeToPickup", estimatedTimeToPickup);
                    response.put("totalRideDistance", totalRideDistance);
                    response.put("estimatedRideTime", estimatedRideTime);
                    // Also send back the overall ride status as perceived by the driver
                    response.put("rideStatus", currentRideStatus);
                    firstUserProcessed = true;
                }
            }
            // response.put("userLocations", new ArrayList<>(users.values())); // Optionally send all user details
        }

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/driver/{slotId}")
    public ResponseEntity<LocationMessage> getDriverLocation(@PathVariable String slotId) {
        if (driverLocationsBySlot.containsKey(slotId)) {
            return ResponseEntity.ok(driverLocationsBySlot.get(slotId));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/users/{slotId}")
    public ResponseEntity<List<LocationMessage>> getUserLocations(@PathVariable String slotId) {
        if (userLocationsBySlot.containsKey(slotId)) {
            return ResponseEntity.ok(new ArrayList<>(userLocationsBySlot.get(slotId).values()));
        } else {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @GetMapping("/userDetails/{slotId}")
    public ResponseEntity<List<LocationMessage>> getUserDetails(@PathVariable String slotId) {
        if (userDetailsBySlot.containsKey(slotId)) {
            return ResponseEntity.ok(new ArrayList<>(userDetailsBySlot.get(slotId).values()));
        } else {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    /**
     * Generate OTP for a specific user
     */
    @PostMapping("/generateOTP")
    public ResponseEntity<Map<String, Object>> generateOTP(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String slotId = request.get("slotId");
        
        Map<String, Object> response = new HashMap<>();
        
        if (userId == null || slotId == null) {
            response.put("status", "error");
            response.put("message", "UserId and SlotId are required");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Generate a random 4-digit OTP
        String otp = LocationUtils.generateOTP();
        
        // Store the OTP for this user
        otpsByUserId.put(userId, otp);
        
        // Update user location message with OTP if it exists
        if (userLocationsBySlot.containsKey(slotId) && 
            userLocationsBySlot.get(slotId).containsKey(userId)) {
            
            LocationMessage userLocation = userLocationsBySlot.get(slotId).get(userId);
            userLocation.setOtp(otp);
            userLocation.setOtpVerified(false);
            userLocationsBySlot.get(slotId).put(userId, userLocation);
        }
        
        response.put("status", "success");
        response.put("userId", userId);
        response.put("otp", otp);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Verify OTP submitted by driver
     */
    @PostMapping("/verifyOTP")
    public ResponseEntity<Map<String, Object>> verifyOTP(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String slotId = request.get("slotId");
        String submittedOtp = request.get("otp");
        
        Map<String, Object> response = new HashMap<>();
        
        if (userId == null || slotId == null || submittedOtp == null) {
            response.put("status", "error");
            response.put("message", "UserId, SlotId, and OTP are required");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Get the stored OTP for this user
        String storedOtp = otpsByUserId.get(userId);
        
        if (storedOtp == null) {
            response.put("status", "error");
            response.put("message", "No OTP found for this user");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean verified = storedOtp.equals(submittedOtp);
        
        // Update user location message with verification status
        if (userLocationsBySlot.containsKey(slotId) && 
            userLocationsBySlot.get(slotId).containsKey(userId)) {
            
            LocationMessage userLocation = userLocationsBySlot.get(slotId).get(userId);
            userLocation.setOtpVerified(verified);
            
            // If verified, update ride status to PICKED_UP
            if (verified) {
                userLocation.setRideStatus("PICKED_UP");
                
                // Also update driver's status if available
                if (driverLocationsBySlot.containsKey(slotId)) {
                    LocationMessage driverLocation = driverLocationsBySlot.get(slotId);
                    driverLocation.setRideStatus("PICKED_UP");
                    driverLocationsBySlot.put(slotId, driverLocation);
                }
            }
            
            userLocationsBySlot.get(slotId).put(userId, userLocation);
        }
        
        response.put("status", verified ? "success" : "error");
        response.put("verified", verified);
        response.put("message", verified ? "OTP verified successfully" : "Invalid OTP");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update ride status (PENDING, ARRIVED, PICKED_UP, DROPPED)
     */
    @PostMapping("/updateStatus")
    public ResponseEntity<Map<String, Object>> updateRideStatus(@RequestBody Map<String, String> request) {
        String slotId = request.get("slotId");
        String driverId = request.get("driverId");
        String userId = request.get("userId"); // Optional, if updating for a specific user
        String status = request.get("status");
        
        Map<String, Object> response = new HashMap<>();
        
        if (slotId == null || driverId == null || status == null) {
            response.put("status", "error");
            response.put("message", "SlotId, DriverId, and Status are required");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Validate status
        if (!status.equals("PENDING") && !status.equals("ARRIVED") && 
            !status.equals("PICKED_UP") && !status.equals("DROPPED")) {
            
            response.put("status", "error");
            response.put("message", "Invalid status. Must be one of: PENDING, ARRIVED, PICKED_UP, DROPPED");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Update driver status
        if (driverLocationsBySlot.containsKey(slotId)) {
            LocationMessage driverLocation = driverLocationsBySlot.get(slotId);
            driverLocation.setRideStatus(status);
            driverLocationsBySlot.put(slotId, driverLocation);
        }
        
        // If userId is provided, update only that user's status
        if (userId != null && userLocationsBySlot.containsKey(slotId) && 
            userLocationsBySlot.get(slotId).containsKey(userId)) {
            
            LocationMessage userLocation = userLocationsBySlot.get(slotId).get(userId);
            userLocation.setRideStatus(status);
            userLocationsBySlot.get(slotId).put(userId, userLocation);
            
            response.put("userId", userId);
        } 
        // Otherwise update all users in the slot
        else if (userLocationsBySlot.containsKey(slotId)) {
            ConcurrentHashMap<String, LocationMessage> users = userLocationsBySlot.get(slotId);
            
            for (String uid : users.keySet()) {
                LocationMessage userLocation = users.get(uid);
                userLocation.setRideStatus(status);
                users.put(uid, userLocation);
            }
        }
        
        response.put("status", "success");
        response.put("message", "Ride status updated to " + status);
        response.put("slotId", slotId);
        response.put("driverId", driverId);
        
        return ResponseEntity.ok(response);
    }
}
