package com.example.demo.Utils;

import java.util.Random;

public class LocationUtils {
    
    // Average driving speed in km/h used for time estimation
    private static final double AVG_DRIVING_SPEED = 40.0;
    
    /**
     * Calculate distance between two points using Haversine formula
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Radius of the earth in km
        final int R = 6371;
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // Distance in km
        return R * c;
    }
    
    /**
     * Estimate travel time based on distance
     * @param distanceKm Distance in kilometers
     * @return Estimated time in minutes
     */
    public static int estimateTravelTime(double distanceKm) {
        // Time = distance / speed (converted to minutes)
        return (int) Math.ceil((distanceKm / AVG_DRIVING_SPEED) * 60);
    }
    
    /**
     * Generate a random 4-digit OTP
     * @return OTP as a string
     */
    public static String generateOTP() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000); // 4-digit number between 1000 and 9999
        return String.valueOf(otp);
    }
}
