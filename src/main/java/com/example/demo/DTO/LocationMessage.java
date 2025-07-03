package com.example.demo.DTO;

import java.time.LocalDateTime;

public class LocationMessage {
    private String userId;
    private String driverId;
    private String slotId;
    private double latitude;
    private double longitude;
    private String pickupLocation;
    private String dropLocation;
    private String messageType; // "USER_LOCATION" or "DRIVER_LOCATION"
    
    // New fields for enhanced functionality
    private String rideStatus; // "PENDING", "ARRIVED", "PICKED_UP", "DROPPED"
    private String otp;
    private boolean otpVerified;
    private double pickupLatitude;
    private double pickupLongitude;
    private double dropLatitude;
    private double dropLongitude;
    private double distanceToPickup; // in kilometers
    private int estimatedTimeToPickup; // in minutes
    private double totalRideDistance; // in kilometers
    private int estimatedRideTime; // in minutes
    private LocalDateTime lastUpdated;

    public LocationMessage() {
        this.rideStatus = "PENDING";
        this.otpVerified = false;
        this.lastUpdated = LocalDateTime.now();
    }

    public LocationMessage(String userId, String driverId, String slotId, double latitude, double longitude, 
                          String pickupLocation, String dropLocation, String messageType) {
        this.userId = userId;
        this.driverId = driverId;
        this.slotId = slotId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.messageType = messageType;
        this.rideStatus = "PENDING";
        this.otpVerified = false;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(String dropLocation) {
        this.dropLocation = dropLocation;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public String getRideStatus() {
        return rideStatus;
    }

    public void setRideStatus(String rideStatus) {
        this.rideStatus = rideStatus;
        this.lastUpdated = LocalDateTime.now();
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public boolean isOtpVerified() {
        return otpVerified;
    }

    public void setOtpVerified(boolean otpVerified) {
        this.otpVerified = otpVerified;
    }

    public double getPickupLatitude() {
        return pickupLatitude;
    }

    public void setPickupLatitude(double pickupLatitude) {
        this.pickupLatitude = pickupLatitude;
    }

    public double getPickupLongitude() {
        return pickupLongitude;
    }

    public void setPickupLongitude(double pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }

    public double getDropLatitude() {
        return dropLatitude;
    }

    public void setDropLatitude(double dropLatitude) {
        this.dropLatitude = dropLatitude;
    }

    public double getDropLongitude() {
        return dropLongitude;
    }

    public void setDropLongitude(double dropLongitude) {
        this.dropLongitude = dropLongitude;
    }

    public double getDistanceToPickup() {
        return distanceToPickup;
    }

    public void setDistanceToPickup(double distanceToPickup) {
        this.distanceToPickup = distanceToPickup;
    }

    public int getEstimatedTimeToPickup() {
        return estimatedTimeToPickup;
    }

    public void setEstimatedTimeToPickup(int estimatedTimeToPickup) {
        this.estimatedTimeToPickup = estimatedTimeToPickup;
    }

    public double getTotalRideDistance() {
        return totalRideDistance;
    }

    public void setTotalRideDistance(double totalRideDistance) {
        this.totalRideDistance = totalRideDistance;
    }

    public int getEstimatedRideTime() {
        return estimatedRideTime;
    }

    public void setEstimatedRideTime(int estimatedRideTime) {
        this.estimatedRideTime = estimatedRideTime;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
