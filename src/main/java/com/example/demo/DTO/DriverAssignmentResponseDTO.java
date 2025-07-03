package com.example.demo.DTO;


import java.time.LocalDate;
import java.util.List;
import com.example.demo.Model.SchedulingBooking;


public class DriverAssignmentResponseDTO {
    private boolean success;
    private String message;
    private String errorCode;
    private SchedulingBooking booking;
    private String nextAvailableTime;
    private List<String> suggestions;

    public DriverAssignmentResponseDTO() {}

    public DriverAssignmentResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public DriverAssignmentResponseDTO(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }

    // Success response
    public static DriverAssignmentResponseDTO success(SchedulingBooking booking, String message) {
        DriverAssignmentResponseDTO response = new DriverAssignmentResponseDTO(true, message);
        response.setBooking(booking);
        return response;
    }

    // Error responses with specific codes
    public static DriverAssignmentResponseDTO routeOverlap(String existingRoute, LocalDate date, List<String> suggestions) {
        DriverAssignmentResponseDTO response = new DriverAssignmentResponseDTO(
                false,
                "Route conflicts with existing booking on " + date + ". Existing route: " + existingRoute,
                "ROUTE_OVERLAP"
        );
        response.setSuggestions(suggestions);
        return response;
    }

    public static DriverAssignmentResponseDTO capacityExceeded(String slotId, int capacity, LocalDate date) {
        return new DriverAssignmentResponseDTO(
                false,
                "Driver is fully booked for " + date + ". Vehicle capacity: " + capacity + " passengers",
                "CAPACITY_EXCEEDED"
        );
    }

    public static DriverAssignmentResponseDTO timeGapViolation(String nextAvailableTime, LocalDate date) {
        DriverAssignmentResponseDTO response = new DriverAssignmentResponseDTO(
                false,
                "Booking time is too close to existing bookings on " + date + ". Please choose a different time.",
                "TIME_GAP_VIOLATION"
        );
        response.setNextAvailableTime(nextAvailableTime);
        return response;
    }

    public static DriverAssignmentResponseDTO cabTypeMismatch(String requiredCabType, String requestedCabType) {
        return new DriverAssignmentResponseDTO(
                false,
                "Driver is assigned to " + requiredCabType + " bookings, but you requested " + requestedCabType,
                "CAB_TYPE_MISMATCH"
        );
    }

    public static DriverAssignmentResponseDTO driverNotFound(int driverId) {
        return new DriverAssignmentResponseDTO(
                false,
                "Driver with ID " + driverId + " not found. Please select a valid driver.",
                "DRIVER_NOT_FOUND"
        );
    }

    public static DriverAssignmentResponseDTO bookingNotFound(int bookingId) {
        return new DriverAssignmentResponseDTO(
                false,
                "Booking with ID " + bookingId + " not found.",
                "BOOKING_NOT_FOUND"
        );
    }

    public static DriverAssignmentResponseDTO invalidTime(String timeString) {
        return new DriverAssignmentResponseDTO(
                false,
                "Invalid time format: '" + timeString + "'. Please use HH:mm format (e.g., 09:30)",
                "INVALID_TIME_FORMAT"
        );
    }

    public static DriverAssignmentResponseDTO missingCabType() {
        return new DriverAssignmentResponseDTO(
                false,
                "Cab type is required for booking assignment. Please specify the vehicle type.",
                "MISSING_CAB_TYPE"
        );
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public SchedulingBooking getBooking() { return booking; }
    public void setBooking(SchedulingBooking booking) { this.booking = booking; }

    public String getNextAvailableTime() { return nextAvailableTime; }
    public void setNextAvailableTime(String nextAvailableTime) { this.nextAvailableTime = nextAvailableTime; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
}