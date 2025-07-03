package com.example.demo.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.example.demo.DTO.*;
import com.example.demo.Service.Location.LocationValidationResult;
import com.example.demo.Service.Location.LocationValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.example.demo.Model.Booking;
import com.example.demo.Model.CarRentalUser;
import com.example.demo.Model.ScheduledDate;
import com.example.demo.Model.SchedulingBooking;
import com.example.demo.Model.Vendor;
import com.example.demo.Model.VendorDriver;
import com.example.demo.Repository.ScheduleBookingRepository;
import com.example.demo.Repository.ScheduleDates;

import jakarta.transaction.Transactional;

@Service
public class ScheduleBookingService {

    @Autowired
    private ScheduleBookingRepository scheduleBookingRepository;

    @Autowired
    private ScheduleDates scheduleDates;

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(ScheduleBookingService.class);
    private final String apiKey = "AIzaSyCelDo4I5cPQ72TfCTQW-arhPZ7ALNcp8w";

    @Transactional
    public MultiDateAssignmentResponseDTO assignDriverBookingWithResponse(int bookingId, int vendorDriverId) {
        MultiDateAssignmentResponseDTO multiResponse = new MultiDateAssignmentResponseDTO();

        try {
            SchedulingBooking newBooking = scheduleBookingRepository.findById(bookingId)
                    .orElse(null);

            if (newBooking == null) {
                DriverAssignmentResponseDTO errorResponse = DriverAssignmentResponseDTO.bookingNotFound(bookingId);
                multiResponse.addDateResult(LocalDate.now(), errorResponse);
                return multiResponse;
            }

            List<LocalDate> bookingDates = newBooking.getScheduledDates().stream()
                    .map(ScheduledDate::getDate)
                    .collect(Collectors.toList());

            for (LocalDate bookingDate : bookingDates) {
                logger.info("Assigning driver {} to booking {} for date {}", vendorDriverId, bookingId, bookingDate);

                try {
                    SchedulingBooking result = assignDriverForSpecificDate(newBooking, vendorDriverId, bookingDate);

                    DriverAssignmentResponseDTO successResponse = DriverAssignmentResponseDTO.success(result,
                            "Driver successfully assigned for " + bookingDate);
                    multiResponse.addDateResult(bookingDate, successResponse);

                } catch (TimeGapViolationException e) {
                    String message = e.getMessage();
                    String nextTime = extractNextAvailableTime(message);
                    DriverAssignmentResponseDTO errorResponse = DriverAssignmentResponseDTO.timeGapViolation(nextTime, bookingDate);
                    multiResponse.addDateResult(bookingDate, errorResponse);

                } catch (SlotCapacityExceededException e) {
                    DriverAssignmentResponseDTO errorResponse = DriverAssignmentResponseDTO.capacityExceeded(
                            e.getSlotId(), e.getCapacity(), bookingDate);
                    multiResponse.addDateResult(bookingDate, errorResponse);

                } catch (RuntimeException e) {
                    String message = e.getMessage();
                    DriverAssignmentResponseDTO errorResponse;

                    if (message.contains("route overlaps with existing booking")) {
                        String existingRoute = extractExistingRoute(message);
                        List<String> suggestions = Arrays.asList(
                                "Try a different pickup time",
                                "Choose a pickup location closer to the existing route",
                                "Select a different driver for this route"
                        );
                        errorResponse = DriverAssignmentResponseDTO.routeOverlap(existingRoute, bookingDate, suggestions);

                    } else if (message.contains("cab type mismatch")) {
                        String[] cabTypes = extractCabTypes(message);
                        errorResponse = DriverAssignmentResponseDTO.cabTypeMismatch(cabTypes[0], cabTypes[1]);

                    } else if (message.contains("Vendor driver not found")) {
                        errorResponse = DriverAssignmentResponseDTO.driverNotFound(vendorDriverId);

                    } else if (message.contains("Invalid time format")) {
                        errorResponse = DriverAssignmentResponseDTO.invalidTime(newBooking.getTime());

                    } else if (message.contains("CabType is required")) {
                        errorResponse = DriverAssignmentResponseDTO.missingCabType();

                    } else {
                        errorResponse = new DriverAssignmentResponseDTO(false, message, "ASSIGNMENT_ERROR");
                    }

                    multiResponse.addDateResult(bookingDate, errorResponse);
                }
            }

            return multiResponse;

        } catch (Exception e) {
            logger.error("Unexpected error during driver assignment: {}", e.getMessage());
            DriverAssignmentResponseDTO errorResponse = new DriverAssignmentResponseDTO(
                    false,
                    "An unexpected error occurred: " + e.getMessage(),
                    "SYSTEM_ERROR"
            );
            multiResponse.addDateResult(LocalDate.now(), errorResponse);
            return multiResponse;
        }
    }

    private String extractNextAvailableTime(String message) {
        try {
            int index = message.indexOf("Next available time: ");
            if (index != -1) {
                return message.substring(index + 21).trim();
            }
        } catch (Exception e) {
            logger.debug("Could not extract next available time from message: {}", message);
        }
        return "Please try a different time";
    }

    private String extractExistingRoute(String message) {
        try {
            int start = message.indexOf("(") + 1;
            int end = message.indexOf(")", start);
            if (start > 0 && end > start) {
                return message.substring(start, end);
            }
        } catch (Exception e) {
            logger.debug("Could not extract existing route from message: {}", message);
        }
        return "existing route";
    }

    private String[] extractCabTypes(String message) {
        try {
            String[] result = new String[2];
            if (message.contains("Slot requires") && message.contains("but booking has")) {
                int requiresStart = message.indexOf("Slot requires ") + 14;
                int requiresEnd = message.indexOf(",", requiresStart);
                int hasStart = message.indexOf("but booking has ") + 16;

                result[0] = message.substring(requiresStart, requiresEnd).trim();
                result[1] = message.substring(hasStart).trim();
                return result;
            }
        } catch (Exception e) {
            logger.debug("Could not extract cab types from message: {}", message);
        }
        return new String[]{"required cab type", "requested cab type"};
    }

    @Transactional
    private SchedulingBooking assignDriverForSpecificDate(SchedulingBooking newBooking, int vendorDriverId, LocalDate bookingDate) {
        logger.info("Processing driver assignment for booking {} on date {}", newBooking.getId(), bookingDate);

        if (newBooking.getCabType() == null) {
            throw new RuntimeException("CabType is required for booking assignment.");
        }

        // Verify driver exists
        String driverUrl = "https://api.worldtriplink.com/vendorDriver/" + vendorDriverId;
        try {
            restTemplate.getForObject(driverUrl, VendorDriver.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Vendor driver not found with ID: " + vendorDriverId);
        }

        LocalTime newBookingTime;
        try {
            newBookingTime = LocalTime.parse(newBooking.getTime(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            throw new RuntimeException("Invalid time format in booking. Expected HH:mm format.");
        }

        logger.info("New booking time: {} for date: {}", newBookingTime, bookingDate);

        // Get existing bookings for this driver and date
        List<SchedulingBooking> existingBookings = getBookingsByDriverAndDate(vendorDriverId, bookingDate);

        // Filter out current booking
        List<SchedulingBooking> filteredBookings = existingBookings.stream()
                .filter(booking -> booking.getId() != newBooking.getId())
                .collect(Collectors.toList());

        logger.info("DEBUG: After filtering current booking {} - Found {} other existing bookings",
                newBooking.getId(), filteredBookings.size());

        if (filteredBookings.isEmpty()) {
            logger.info("No existing bookings for driver {} on date {}. Creating new slot.", vendorDriverId, bookingDate);
            return createNewSlotAndAssign(newBooking, vendorDriverId, bookingDate, newBookingTime);
        }

        logger.info("Found {} existing bookings for driver {} on date {}", filteredBookings.size(), vendorDriverId, bookingDate);

        // Group existing bookings by their slot IDs for this specific date
        Map<String, List<SchedulingBooking>> slotGroups = groupBookingsBySlotForDate(filteredBookings, bookingDate);

        logger.info("Found {} different slots for driver {} on date {}", slotGroups.size(), vendorDriverId, bookingDate);

        String activeSlotId = null;
        List<SchedulingBooking> activeSlotBookings = null;

        for (Map.Entry<String, List<SchedulingBooking>> slotEntry : slotGroups.entrySet()) {
            String slotId = slotEntry.getKey();
            List<SchedulingBooking> slotBookings = slotEntry.getValue();

            LocalTime slotEnd = getSlotEndTime(slotBookings);

            logger.info("Checking slot {} on date {} - ends at {}, new booking time: {}",
                    slotId, bookingDate, slotEnd, newBookingTime);

            // Check if new booking time is AFTER slot end time (slot has expired for this booking)
            if (newBookingTime.isAfter(slotEnd)) {
                logger.info("Slot {} has expired for booking time {} on date {} (slot ended at {})",
                        slotId, newBookingTime, bookingDate, slotEnd);
                continue;
            } else {
                logger.info("Found active slot: {} on date {} (booking time {} is before/at slot end {})",
                        slotId, bookingDate, newBookingTime, slotEnd);
                activeSlotId = slotId;
                activeSlotBookings = slotBookings;
                break;
            }
        }

        if (activeSlotId == null) {
            logger.info("No active slot found for driver {} on date {} and booking time {}. Creating new slot.",
                    vendorDriverId, bookingDate, newBookingTime);
            return createNewSlotAndAssign(newBooking, vendorDriverId, bookingDate, newBookingTime);
        }

        logger.info("Found active slot {} with {} bookings for driver {} on date {}",
                activeSlotId, activeSlotBookings.size(), vendorDriverId, bookingDate);

        LocalTime slotStart = getSlotStartTime(activeSlotBookings);
        LocalTime slotEnd = getSlotEndTime(activeSlotBookings);

        logger.info("Active slot details for date {} - Start: {}, End: {}, New booking time: {}",
                bookingDate, slotStart, slotEnd, newBookingTime);

        // Check 20-minute gap BEFORE capacity check
        LocalTime minimumAllowedTime = slotStart.plusMinutes(20);
        logger.info("20-minute gap check for date {} - Slot start: {}, Minimum allowed: {}, New booking: {}",
                bookingDate, slotStart, minimumAllowedTime, newBookingTime);

        // Allow booking at slot start time or after the 20-minute gap
        if (newBookingTime.isAfter(slotStart) && newBookingTime.isBefore(minimumAllowedTime)) {
            throw new TimeGapViolationException(
                    "Booking time " + newBookingTime + " is within 20-minute gap from slot start " +
                            slotStart + " on date " + bookingDate + ". Next available time: " + minimumAllowedTime);
        }

        // 20-minute gap check passed - try to add to existing slot
        logger.info("20-minute gap check passed for date {}. Attempting to add to existing slot {}.", bookingDate, activeSlotId);
        return assignToExistingSlotForDate(newBooking, vendorDriverId, bookingDate, newBookingTime,
                activeSlotId, activeSlotBookings, slotStart);
    }

    private Map<String, List<SchedulingBooking>> groupBookingsBySlotForDate(List<SchedulingBooking> bookings, LocalDate date) {
        Map<String, List<SchedulingBooking>> slotGroups = new HashMap<>();

        for (SchedulingBooking booking : bookings) {
            // Find the scheduled date entry for this specific date
            ScheduledDate scheduledDate = booking.getScheduledDates().stream()
                    .filter(sd -> sd.getDate().equals(date))
                    .findFirst()
                    .orElse(null);

            if (scheduledDate != null && scheduledDate.getSlotId() != null) {
                String slotId = scheduledDate.getSlotId();
                slotGroups.computeIfAbsent(slotId, k -> new ArrayList<>()).add(booking);
            }
        }

        return slotGroups;
    }

    private List<SchedulingBooking> getBookingsByDriverAndDate(int vendorDriverId, LocalDate date) {
        return scheduleBookingRepository.findByVendorDriverIdAndScheduledDatesDate(vendorDriverId, date);
    }

    private SchedulingBooking createNewSlotAndAssign(SchedulingBooking newBooking, int vendorDriverId,
                                                     LocalDate bookingDate, LocalTime bookingTime) {
        logger.info("Creating new slot for driver {} with booking time {} on date {}", vendorDriverId, bookingTime, bookingDate);

        String newSlotId = generateNewSlotId(vendorDriverId, bookingDate, bookingTime);

        ScheduledDate targetScheduledDate = newBooking.getScheduledDates().stream()
                .filter(sd -> sd.getDate().equals(bookingDate))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ScheduledDate not found for booking " + newBooking.getId() + " and date " + bookingDate));

        targetScheduledDate.setSlotId(newSlotId);

        newBooking.setVendorDriverId(vendorDriverId);

        logger.info("Created new slot {} for date {} on scheduled date ID {}", newSlotId, bookingDate, targetScheduledDate.getId());

        // Save the booking (this will cascade to scheduled dates)
        return scheduleBookingRepository.save(newBooking);
    }

    private String generateNewSlotId(int vendorDriverId, LocalDate bookingDate, LocalTime bookingTime) {
        return "SLOT_" + vendorDriverId + "_" +
                bookingDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                bookingTime.format(DateTimeFormatter.ofPattern("HHmm")) + "_" +
                System.currentTimeMillis();
    }

    // UPDATED METHOD: Assign to existing slot for specific date
    private SchedulingBooking assignToExistingSlotForDate(SchedulingBooking newBooking, int vendorDriverId,
                                                          LocalDate bookingDate, LocalTime bookingTime,
                                                          String slotId, List<SchedulingBooking> slotBookings,
                                                          LocalTime slotStart) {

        // Check cab type compatibility
        if (!slotBookings.isEmpty()) {
            String existingCabType = slotBookings.get(0).getCabType();
            String requestingCabType = newBooking.getCabType();

            if (existingCabType == null || requestingCabType == null) {
                logger.error("Null cabType found during comparison");
                throw new RuntimeException("CabType cannot be null for slot assignment");
            }

            if (!existingCabType.equalsIgnoreCase(requestingCabType)) {
                logger.debug("Cab type mismatch - existing: {}, requesting: {}", existingCabType, requestingCabType);
                throw new RuntimeException("Cannot assign: cab type mismatch. Slot requires " +
                        existingCabType + ", but booking has " + requestingCabType);
            }
        }

        int availableSeats = getAvailableSeatsForCabType(newBooking.getCabType());
        if (availableSeats == -1) {
            throw new RuntimeException("Unknown cab type: " + newBooking.getCabType());
        }

        int usedSeats = slotBookings.size();
        int remainingSeats = availableSeats - usedSeats;

        logger.debug("Capacity check for date {} - Available seats: {}, Used seats: {}, Remaining seats: {}",
                bookingDate, availableSeats, usedSeats, remainingSeats);

        if (remainingSeats < 1) {
            throw new SlotCapacityExceededException(
                    "Cannot assign: slot " + slotId + " is at capacity on date " + bookingDate + ". " +
                            "Available: " + availableSeats + " seats, " +
                            "Used: " + usedSeats + " seats, " +
                            "Remaining: " + remainingSeats + " seats",
                    slotId, availableSeats);
        }

        // Check route overlap with existing bookings
        for (SchedulingBooking existingBooking : slotBookings) {
            if (isRouteOverlapping(
                    existingBooking.getPickUpLocation(),
                    existingBooking.getDropLocation(),
                    newBooking.getPickUpLocation(),
                    newBooking.getDropLocation())) {
                throw new RuntimeException(
                        "Cannot assign driver - route overlaps with existing booking ID: " +
                                existingBooking.getId() + " (" + existingBooking.getPickUpLocation() +
                                " to " + existingBooking.getDropLocation() + ") on date " + bookingDate);
            }
        }

        // Find the specific scheduled date and assign slot ID to it
        ScheduledDate targetScheduledDate = newBooking.getScheduledDates().stream()
                .filter(sd -> sd.getDate().equals(bookingDate))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ScheduledDate not found for booking " + newBooking.getId() + " and date " + bookingDate));

        targetScheduledDate.setSlotId(slotId);

        newBooking.setVendorDriverId(vendorDriverId);

        logger.info("Successfully assigned booking {} to slot {} for driver {} on date {}. " +
                        "Slot now has {}/{} seats used",
                newBooking.getId(), slotId, vendorDriverId, bookingDate,
                usedSeats + 1, availableSeats);

        return scheduleBookingRepository.save(newBooking);
    }

    public boolean isRouteOverlapping(String existingPickup, String existingDrop, String newPickup, String newDrop) {
        try {
            logger.info("Checking route overlap between [{}→{}] and [{}→{}]",
                    existingPickup, existingDrop, newPickup, newDrop);

            // Rule 1: New pickup must be along existing route (no backtracking)
            if (!isPickupAlongRoute(existingPickup, existingDrop, newPickup)) {
                logger.info("Route overlap detected: new pickup {} requires backtracking from route {}→{}",
                        newPickup, existingPickup, existingDrop);
                return true; // Overlap = reject
            }

            // Rule 2: New drop must be in same general direction
            if (!isSameGeneralDirection(existingPickup, existingDrop, newPickup, newDrop)) {
                logger.info("Route overlap detected: routes go in different directions");
                return true; // Overlap = reject
            }

            // Rule 3: Overall route should be reasonably efficient
            if (!isOverallRouteEfficient(existingPickup, existingDrop, newPickup, newDrop)) {
                logger.info("Route overlap detected: combined route is inefficient");
                return true; // Overlap = reject
            }

            logger.info("No route overlap detected - routes can be efficiently combined");
            return false; // No overlap = accept

        } catch (Exception e) {
            logger.error("Error checking route overlap: " + e.getMessage());
            // In case of error, be conservative and assume overlap
            return true;
        }
    }

    private boolean isPickupAlongRoute(String existingPickup, String existingDrop, String newPickup) {
        try {
            // Get distances
            double existingRouteDistance = getDirectRouteDistance(existingPickup, existingDrop);
            double pickupToStart = getDirectRouteDistance(existingPickup, newPickup);
            double pickupToEnd = getDirectRouteDistance(newPickup, existingDrop);

            // New pickup should be roughly between start and end
            // Allow 30% tolerance for slight detours
            double tolerance = 1.3;
            boolean isAlong = (pickupToStart + pickupToEnd) <= (existingRouteDistance * tolerance);

            logger.debug("Pickup along route check - Existing: {:.2f}km, Via pickup: {:.2f}km, Along route: {}",
                    existingRouteDistance / 1000, (pickupToStart + pickupToEnd) / 1000, isAlong);

            return isAlong;

        } catch (Exception e) {
            logger.error("Error checking pickup along route: " + e.getMessage());
            return false;
        }
    }

    private boolean isSameGeneralDirection(String existingPickup, String existingDrop, String newPickup, String newDrop) {
        try {
            // Get coordinates
            double[] coordsExistingPickup = getCoordinates(existingPickup);
            double[] coordsExistingDrop = getCoordinates(existingDrop);
            double[] coordsNewPickup = getCoordinates(newPickup);
            double[] coordsNewDrop = getCoordinates(newDrop);

            if (coordsExistingPickup == null || coordsExistingDrop == null ||
                    coordsNewPickup == null || coordsNewDrop == null) {
                return false;
            }

            // Calculate direction vectors
            double existingDirectionLat = coordsExistingDrop[0] - coordsExistingPickup[0];
            double existingDirectionLng = coordsExistingDrop[1] - coordsExistingPickup[1];
            double newDirectionLat = coordsNewDrop[0] - coordsNewPickup[0];
            double newDirectionLng = coordsNewDrop[1] - coordsNewPickup[1];

            // Calculate angle between direction vectors
            double dotProduct = existingDirectionLat * newDirectionLat + existingDirectionLng * newDirectionLng;
            double existingMagnitude = Math.sqrt(existingDirectionLat * existingDirectionLat + existingDirectionLng * existingDirectionLng);
            double newMagnitude = Math.sqrt(newDirectionLat * newDirectionLat + newDirectionLng * newDirectionLng);

            if (existingMagnitude == 0 || newMagnitude == 0) {
                return false;
            }

            double cosAngle = dotProduct / (existingMagnitude * newMagnitude);
            double angle = Math.acos(Math.max(-1.0, Math.min(1.0, cosAngle)));
            double angleDegrees = Math.toDegrees(angle);

            boolean sameDirection = angleDegrees <= 60.0;

            logger.debug("Direction check - Angle: {:.1f}°, Same direction: {}", angleDegrees, sameDirection);

            return sameDirection;

        } catch (Exception e) {
            logger.error("Error checking same direction: " + e.getMessage());
            return false;
        }
    }

    private boolean isOverallRouteEfficient(String existingPickup, String existingDrop, String newPickup, String newDrop) {
        try {

            // Calculate separate route distances
            double existingRouteDistance = getDirectRouteDistance(existingPickup, existingDrop);
            double newRouteDistance = getDirectRouteDistance(newPickup, newDrop);
            double separateTotal = existingRouteDistance + newRouteDistance;

            // Calculate optimal combined route distance
            // Try different sequences to find most efficient
            double[] sequences = {
                    getCombinedRouteDistance(existingPickup, newPickup, existingDrop, newDrop),
                    getCombinedRouteDistance(existingPickup, newPickup, newDrop, existingDrop),
                    getCombinedRouteDistance(newPickup, existingPickup, existingDrop, newDrop),
                    getCombinedRouteDistance(newPickup, existingPickup, newDrop, existingDrop)
            };

            double bestCombinedDistance = Double.MAX_VALUE;
            for (double distance : sequences) {
                if (distance > 0 && distance < bestCombinedDistance) {
                    bestCombinedDistance = distance;
                }
            }

            if (bestCombinedDistance == Double.MAX_VALUE) {
                return false;
            }

            // Check efficiency - combined route should not be more than 50% longer than separate routes
            double efficiency = bestCombinedDistance / separateTotal;
            boolean isEfficient = efficiency <= 1.5;

            logger.debug("Efficiency check - Separate: {:.2f}km, Combined: {:.2f}km, Efficiency: {:.1f}%, Efficient: {}",
                    separateTotal / 1000, bestCombinedDistance / 1000, efficiency * 100, isEfficient);

            return isEfficient;

        } catch (Exception e) {
            logger.error("Error checking route efficiency: " + e.getMessage());
            return false;
        }
    }

    private double getCombinedRouteDistance(String point1, String point2, String point3, String point4) {
        try {
            double dist1to2 = getDirectRouteDistance(point1, point2);
            double dist2to3 = getDirectRouteDistance(point2, point3);
            double dist3to4 = getDirectRouteDistance(point3, point4);

            return dist1to2 + dist2to3 + dist3to4;

        } catch (Exception e) {
            logger.error("Error calculating combined route distance: " + e.getMessage());
            return -1;
        }
    }

    public double[] getCoordinates(String location) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://maps.googleapis.com/maps/api/geocode/json")
                    .queryParam("address", UriUtils.encode(location, "UTF-8"))
                    .queryParam("key", apiKey)
                    .build()
                    .toString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "OK".equals(response.get("status"))) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                if (results != null && !results.isEmpty()) {
                    Map<String, Object> geometry = (Map<String, Object>) results.get(0).get("geometry");
                    Map<String, Object> locationMap = (Map<String, Object>) geometry.get("location");

                    double lat = ((Number) locationMap.get("lat")).doubleValue();
                    double lng = ((Number) locationMap.get("lng")).doubleValue();

                    return new double[]{lat, lng};
                }
            }

            return null;

        } catch (Exception e) {
            logger.error("Error getting coordinates for location: " + location + " - " + e.getMessage());
            return null;
        }
    }

    private double getDirectRouteDistance(String origin, String destination) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://maps.googleapis.com/maps/api/directions/json")
                    .queryParam("origin", UriUtils.encode(origin, "UTF-8"))
                    .queryParam("destination", UriUtils.encode(destination, "UTF-8"))
                    .queryParam("key", apiKey)
                    .build()
                    .toString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "OK".equals(response.get("status"))) {
                List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");
                if (routes != null && !routes.isEmpty()) {
                    Map<String, Object> route = routes.get(0);
                    List<Map<String, Object>> legs = (List<Map<String, Object>>) route.get("legs");

                    if (legs != null && !legs.isEmpty()) {
                        Map<String, Object> distance = (Map<String, Object>) legs.get(0).get("distance");
                        if (distance != null) {
                            return ((Number) distance.get("value")).doubleValue();
                        }
                    }
                }
            }

            return 0;

        } catch (Exception e) {
            logger.error("Error getting direct route distance: " + e.getMessage());
            return 0;
        }
    }

    private int getAvailableSeatsForCabType(String cabType) {
        if (cabType == null) return -1;

        switch (cabType.toLowerCase()) {
            case "suv":
                return 4;
            case "sedan":
            case "sedan premium":
            case "hatchback":
                return 3;
            default:
                return -1;
        }
    }

    private LocalTime getSlotStartTime(List<SchedulingBooking> slotBookings) {
        return slotBookings.stream()
                .map(booking -> LocalTime.parse(booking.getTime(), DateTimeFormatter.ofPattern("HH:mm")))
                .min(LocalTime::compareTo)
                .orElse(LocalTime.now());
    }

    private LocalTime getSlotEndTime(List<SchedulingBooking> slotBookings) {
        LocalTime latestPickup = slotBookings.stream()
                .map(booking -> LocalTime.parse(booking.getTime(), DateTimeFormatter.ofPattern("HH:mm")))
                .max(LocalTime::compareTo)
                .orElse(LocalTime.now());

        return latestPickup.plusHours(1).plusMinutes(30);
    }

    // UPDATED METHOD: Get driver slots with new logic
    public DriverSlotsResponseDTO getDriverSlots(int vendorDriverId) {
        try {
            String driverUrl = "https://api.worldtriplink.com/vendorDriver/" + vendorDriverId;
            try {
                restTemplate.getForObject(driverUrl, VendorDriver.class);
            } catch (HttpClientErrorException.NotFound e) {
                throw new RuntimeException("Vendor driver not found with ID: " + vendorDriverId);
            }

            List<SchedulingBooking> allBookings = scheduleBookingRepository.findByVendorDriverId(vendorDriverId);

            if (allBookings.isEmpty()) {
                return new DriverSlotsResponseDTO(new ArrayList<>());
            }

            // Group bookings by slot and date using ScheduledDate.slotId
            Map<String, List<BookingDatePair>> groupedBookings = groupBookingsBySlotAndDateNew(allBookings);

            // Convert to DTOs and sort
            List<SlotDTO> slots = convertToSlotDTOs(groupedBookings);

            // Sort by latest date first
            slots.sort((slot1, slot2) -> slot2.getDate().compareTo(slot1.getDate()));

            return new DriverSlotsResponseDTO(slots);

        } catch (Exception e) {
            logger.error("Error fetching driver slots for vendorDriverId {}: {}", vendorDriverId, e.getMessage());
            throw new RuntimeException("Error fetching driver slots: " + e.getMessage());

        }
    }

    // UPDATED METHOD: Group bookings using ScheduledDate.slotId
    private Map<String, List<BookingDatePair>> groupBookingsBySlotAndDateNew(List<SchedulingBooking> bookings) {
        Map<String, List<BookingDatePair>> grouped = new HashMap<>();

        for (SchedulingBooking booking : bookings) {
            for (ScheduledDate scheduledDate : booking.getScheduledDates()) {
                // Use slotId from ScheduledDate, not from SchedulingBooking
                String slotId = scheduledDate.getSlotId();
                String key = createGroupingKey(slotId, scheduledDate.getDate());
                BookingDatePair pair = new BookingDatePair(booking, scheduledDate);
                grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(pair);
            }
        }

        return grouped;
    }

    // Helper class to pair booking with specific date
    private static class BookingDatePair {
        private final SchedulingBooking booking;
        private final ScheduledDate scheduledDate;

        public BookingDatePair(SchedulingBooking booking, ScheduledDate scheduledDate) {
            this.booking = booking;
            this.scheduledDate = scheduledDate;
        }

        public SchedulingBooking getBooking() {
            return booking;
        }

        public ScheduledDate getScheduledDate() {
            return scheduledDate;
        }
    }

    private String createGroupingKey(String slotId, LocalDate date) {
        String slot = (slotId != null) ? slotId : "NULL_SLOT";
        return slot + "_" + date.toString();
    }

    private List<SlotDTO> convertToSlotDTOs(Map<String, List<BookingDatePair>> groupedBookings) {
        List<SlotDTO> slots = new ArrayList<>();

        for (Map.Entry<String, List<BookingDatePair>> entry : groupedBookings.entrySet()) {
            String key = entry.getKey();
            List<BookingDatePair> bookingPairs = entry.getValue();

            // Parse key to get slotId and date
            String[] parts = key.split("_(?=\\d{4}-\\d{2}-\\d{2}$)"); // Split at date pattern
            String slotId = parts[0].equals("NULL_SLOT") ? null : parts[0];
            LocalDate date = LocalDate.parse(parts[1]);

            // Convert bookings to DTOs
            List<SlotBookingDTO> bookingDTOs = bookingPairs.stream()
                    .map(pair -> convertToSlotBookingDTO(pair.getBooking(), pair.getScheduledDate()))
                    .sorted((b1, b2) -> b1.getPickupTime().compareTo(b2.getPickupTime())) // Sort by pickup time
                    .collect(Collectors.toList());

            SlotDTO slotDTO = new SlotDTO(slotId, date, bookingDTOs.size(), bookingDTOs);
            slots.add(slotDTO);
        }

        return slots;
    }

    private SlotBookingDTO convertToSlotBookingDTO(SchedulingBooking booking, ScheduledDate scheduledDate) {
        String status = (scheduledDate.getStatus() != null) ? scheduledDate.getStatus() : "PENDING";

        int userId = booking.getCarRentalUserId();

        int vendorId = 0;
        if (booking.getVendorId() != null) {
            vendorId = booking.getVendorId().intValue();
        }

        int vendorDriverId = booking.getVendorDriverId();

        String userServiceUrl = "https://api.worldtriplink.com/auth/getCarRentalUserById/" + userId;
        CarRentalUser user = restTemplate.getForObject(userServiceUrl, CarRentalUser.class);
        System.out.println(user);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }


//        String userName=user.getUserName();
//        System.out.println(userName);
//        String phone=user.getPhone();
//        System.out.println(phone);


        return new SlotBookingDTO(
                userId,
                vendorId,
                vendorDriverId,
                booking.getId(),
                booking.getTime(),
                booking.getPickUpLocation(),
                booking.getDropLocation(),
                status,
                user.getUserName(),
                user.getPhone()
        );
    }

    public static class SlotCapacityExceededException extends RuntimeException {
        private final String slotId;
        private final int capacity;

        public SlotCapacityExceededException(String message, String slotId, int capacity) {
            super(message);
            this.slotId = slotId;
            this.capacity = capacity;
        }

        public String getSlotId() {
            return slotId;
        }

        public int getCapacity() {
            return capacity;
        }
    }

    public static class TimeGapViolationException extends RuntimeException {
        public TimeGapViolationException(String message) {
            super(message);
        }
    }

    public enum DriverState {
        AVAILABLE,      // No active slot or slot expired
        SLOT_ACTIVE,    // Has active slot with available capacity
        SLOT_EXPIRED    // Slot has expired, ready for new slot
    }

    public static class DriverSlotStatus {
        private final DriverState state;
        private final String slotId;
        private final LocalTime slotStart;
        private final LocalTime slotEnd;
        private final LocalDate slotDate;

        public DriverSlotStatus(DriverState state, String slotId, LocalTime slotStart, LocalTime slotEnd, LocalDate slotDate) {
            this.state = state;
            this.slotId = slotId;
            this.slotStart = slotStart;
            this.slotEnd = slotEnd;
            this.slotDate = slotDate;
        }

        public DriverState getState() {
            return state;
        }

        public String getSlotId() {
            return slotId;
        }

        public LocalTime getSlotStart() {
            return slotStart;
        }

        public LocalTime getSlotEnd() {
            return slotEnd;
        }

        public LocalDate getSlotDate() {
            return slotDate;
        }
    }

    @Transactional
    public SchedulingBooking createSchedule(
            int userId,
            String pickUpLocation,
            String dropLocation,
            String time,
            String returnTime,
            String shiftTime,
            List<LocalDate> dates
    ) {
        String userServiceUrl = "https://api.worldtriplink.com/auth/getCarRentalUserById/" + userId;
        CarRentalUser user = restTemplate.getForObject(userServiceUrl, CarRentalUser.class);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        SchedulingBooking booking = new SchedulingBooking();
        booking.setPickUpLocation(pickUpLocation);
        booking.setDropLocation(dropLocation);
        booking.setTime(time);
        booking.setReturnTime(returnTime);
        booking.setShiftTime(shiftTime);
        booking.setUser(user);

        for (LocalDate date : dates) {
            ScheduledDate sd = new ScheduledDate();
            sd.setDate(date);
            sd.setSchedulingBooking(booking);
            booking.getScheduledDates().add(sd);
        }

        return scheduleBookingRepository.save(booking);
    }

    public SchedulingBooking assignVendorToBooking(int bookingId, Long vendorId) {
        SchedulingBooking booking = scheduleBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        String vendorUrl = "https://api.worldtriplink.com/vendors/" + vendorId;
        try {
            restTemplate.getForObject(vendorUrl, Vendor.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Vendor not found with ID: " + vendorId);
        }

        booking.setVendorId(vendorId);
        return scheduleBookingRepository.save(booking);
    }

    public SchedulingBookingDTO getBookingWithVendorDTO(int bookingId) {
        SchedulingBooking booking = scheduleBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        String vendorServiceUrl = "https://api.worldtriplink.com/vendors/" + booking.getVendorId();
        String vendorDriverServiceUrl = "https://api.worldtriplink.com/vendorDriver/" + booking.getVendorDriverId();
        String userServiceUrl = "https://api.worldtriplink.com/auth/getCarRentalUserById/" + booking.getUser().getId();

        Vendor vendor = restTemplate.getForObject(vendorServiceUrl, Vendor.class);
        VendorDriver vendorDriver = restTemplate.getForObject(vendorDriverServiceUrl, VendorDriver.class);
        CarRentalUser user = restTemplate.getForObject(userServiceUrl, CarRentalUser.class);

        booking.setVendor(vendor);
        booking.setVendorDriver(vendorDriver);

        SchedulingBookingDTO dto = new SchedulingBookingDTO();
        dto.setId(booking.getId());
        dto.setPickUpLocation(booking.getPickUpLocation());
        dto.setDropLocation(booking.getDropLocation());
        dto.setTime(booking.getTime());
        dto.setReturnTime(booking.getReturnTime());
        dto.setShiftTime(booking.getShiftTime());
        dto.setBookingType(booking.getBookingType());
        dto.setDateOfList(booking.getDateOfList());

        VendorDTO vendorDTO = new VendorDTO();
        vendorDTO.setId(vendor.getId());
        vendorDTO.setVendorCompanyName(vendor.getVendorCompanyName());
        vendorDTO.setContactNo(vendor.getContactNo());
        vendorDTO.setAlternateMobileNo(vendor.getAlternateMobileNo());
        vendorDTO.setCity(vendor.getCity());
        vendorDTO.setVendorEmail(vendor.getVendorEmail());
        dto.setVendor(vendorDTO);

        VendorDriverDTO driverDTO = new VendorDriverDTO();
        driverDTO.setVendorDriverId(vendorDriver.getVendorDriverId());
        driverDTO.setDriverName(vendorDriver.getDriverName());
        driverDTO.setContactNo(vendorDriver.getContactNo());
        driverDTO.setAltContactNo(vendorDriver.getAltContactNo());
        dto.setVendorDriver(driverDTO);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUserName(user.getUserName());
        userDTO.setLastName(user.getLastName());
        userDTO.setEmail(user.getEmail());
        userDTO.setGender(user.getGender());
        userDTO.setPhone(user.getPhone());
        dto.setUser(userDTO);

        List<ScheduleDateBookingDTO> scheduledDateDTOs = booking.getScheduledDates().stream().map(sd -> {
            ScheduleDateBookingDTO sdDTO = new ScheduleDateBookingDTO();
            sdDTO.setId(sd.getId());
            sdDTO.setDate(sd.getDate());
            return sdDTO;
        }).toList();
        dto.setScheduledDates(scheduledDateDTOs);

        return dto;
    }

    public List<SchedulingBooking> getBookingByUserId(int userId) {

        return this.scheduleBookingRepository.findByCarRentalUserId(userId);
    }

    public SchedulingBookingDTO getByScheduleBookingId(int id) {
        SchedulingBooking schedulingBooking = this.scheduleBookingRepository.findById(id).get();
        SchedulingBookingDTO dtoS = new SchedulingBookingDTO();
        dtoS.setId(schedulingBooking.getId());
        dtoS.setBookingId(schedulingBooking.getBookId());
        dtoS.setPickUpLocation(schedulingBooking.getPickUpLocation());
        dtoS.setDropLocation(schedulingBooking.getDropLocation());
        dtoS.setTime(schedulingBooking.getTime());
        dtoS.setReturnTime(schedulingBooking.getReturnTime());
        dtoS.setShiftTime(schedulingBooking.getShiftTime());
        dtoS.setBookingType(schedulingBooking.getBookingType());
        try {
            String vendorServiceUrl = "https://api.worldtriplink.com/vendors/" + schedulingBooking.getVendorId();
            Vendor vendor = restTemplate.getForObject(vendorServiceUrl, Vendor.class);
            if (vendor != null) {
                VendorDTO vendorDTO = new VendorDTO();
                vendorDTO.setId(vendor.getId());
                vendorDTO.setVendorCompanyName(vendor.getVendorCompanyName());
                vendorDTO.setContactNo(vendor.getContactNo());
                vendorDTO.setAlternateMobileNo(vendor.getAlternateMobileNo());
                vendorDTO.setCity(vendor.getCity());
                vendorDTO.setVendorEmail(vendor.getVendorEmail());
                dtoS.setVendor(vendorDTO);
            }
        } catch (Exception e) {
            System.out.println("Vendor not found for ID: " + schedulingBooking.getVendorId());
            dtoS.setVendor(null);
        }

        try {
            String vendorDriverServiceUrl = "https://api.worldtriplink.com/vendorDriver/" + schedulingBooking.getVendorDriverId();
            VendorDriver vendorDriver = restTemplate.getForObject(vendorDriverServiceUrl, VendorDriver.class);
            if (vendorDriver != null) {
                VendorDriverDTO driverDTO = new VendorDriverDTO();
                driverDTO.setVendorDriverId(vendorDriver.getVendorDriverId());
                driverDTO.setDriverName(vendorDriver.getDriverName());
                driverDTO.setContactNo(vendorDriver.getContactNo());
                driverDTO.setAltContactNo(vendorDriver.getAltContactNo());
                dtoS.setVendorDriver(driverDTO);
            }
        } catch (Exception e) {
            System.out.println("VendorDriver not found for ID: " + schedulingBooking.getVendorDriverId());
            dtoS.setVendorDriver(null);
        }

        try {
            String userServiceUrl = "https://api.worldtriplink.com/auth/getCarRentalUserById/" + schedulingBooking.getUser().getId();
            CarRentalUser user = restTemplate.getForObject(userServiceUrl, CarRentalUser.class);
            if (user != null) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setUserName(user.getUserName());
                userDTO.setLastName(user.getLastName());
                userDTO.setEmail(user.getEmail());
                userDTO.setGender(user.getGender());
                userDTO.setPhone(user.getPhone());
                dtoS.setUser(userDTO);
            }
        } catch (Exception e) {
            System.out.println("User not found for ID: " + schedulingBooking.getUser().getId());
            dtoS.setUser(null);
        }

        List<ScheduleDateBookingDTO> scheduledDateDTOs = schedulingBooking.getScheduledDates().stream().map(sd -> {
            ScheduleDateBookingDTO sdDTO = new ScheduleDateBookingDTO();
            sdDTO.setId(sd.getId());
            sdDTO.setDate(sd.getDate());
            return sdDTO;
        }).toList();
        dtoS.setScheduledDates(scheduledDateDTOs);

        return dtoS;
    }

    public List<SchedulingBookingDTO> findByUserId(int userId) {
        List<SchedulingBooking> bookings = this.scheduleBookingRepository.findByCarRentalUserId(userId);
        List<SchedulingBookingDTO> dtoList = new ArrayList<>();

        logger.info("Found {} bookings for user {}", bookings.size(), userId);
        for (SchedulingBooking booking : bookings) {
            logger.info("Booking ID: {}, VendorId: {}, VendorDriverId: {}, BookId: {}",
                    booking.getId(),
                    booking.getVendorId(),
                    booking.getVendorDriverId(),
                    booking.getBookId());
        }


        for (SchedulingBooking schedulingBooking : bookings) {
            SchedulingBookingDTO schedulingBookingDTO = new SchedulingBookingDTO();
            schedulingBookingDTO.setId(schedulingBooking.getId());
            schedulingBookingDTO.setBookingId(schedulingBooking.getBookId()); // Set booking ID
            schedulingBookingDTO.setPickUpLocation(schedulingBooking.getPickUpLocation());
            schedulingBookingDTO.setDropLocation(schedulingBooking.getDropLocation());
            schedulingBookingDTO.setTime(schedulingBooking.getTime());
            schedulingBookingDTO.setReturnTime(schedulingBooking.getReturnTime());
            schedulingBookingDTO.setShiftTime(schedulingBooking.getShiftTime());
            schedulingBookingDTO.setBookingType(schedulingBooking.getBookingType());

            // Debug: Log vendorId and vendorDriverId
            logger.info("Processing booking ID: {}, vendorId: {}, vendorDriverId: {}",
                    schedulingBooking.getId(), schedulingBooking.getVendorId(), schedulingBooking.getVendorDriverId());

            // Handle Vendor
            if (schedulingBooking.getVendorId() != null) {
                try {
                    String vendorServiceUrl = "https://api.worldtriplink.com/vendors/" + schedulingBooking.getVendorId();
                    logger.info("Calling vendor API: {}", vendorServiceUrl);
                    Vendor vendor = restTemplate.getForObject(vendorServiceUrl, Vendor.class);

                    if (vendor != null) {
                        VendorDTO vendorDTO = new VendorDTO();
                        vendorDTO.setId(vendor.getId());
                        vendorDTO.setVendorCompanyName(vendor.getVendorCompanyName());
                        vendorDTO.setContactNo(vendor.getContactNo());
                        vendorDTO.setAlternateMobileNo(vendor.getAlternateMobileNo());
                        vendorDTO.setCity(vendor.getCity());
                        vendorDTO.setVendorEmail(vendor.getVendorEmail());
                        schedulingBookingDTO.setVendor(vendorDTO);
                        logger.info("Vendor info set successfully for booking {}", schedulingBooking.getId());
                    } else {
                        logger.warn("Vendor API returned null for ID: {}", schedulingBooking.getVendorId());
                    }
                } catch (Exception e) {
                    logger.error("Error fetching vendor for ID: {} - {}", schedulingBooking.getVendorId(), e.getMessage());
                    schedulingBookingDTO.setVendor(null);
                }
            } else {
                logger.info("No vendorId found for booking {}", schedulingBooking.getId());
                schedulingBookingDTO.setVendor(null);
            }


            if (schedulingBooking.getVendorDriverId() != 0) {
                try {
                    String vendorDriverServiceUrl = "https://api.worldtriplink.com/vendorDriver/" + schedulingBooking.getVendorDriverId();
                    VendorDriver vendorDriver = restTemplate.getForObject(vendorDriverServiceUrl, VendorDriver.class);

                    if (vendorDriver != null) {
                        VendorDriverDTO driverDTO = new VendorDriverDTO();

                        // Use the booking's vendorDriverId since the response field might be null
                        driverDTO.setVendorDriverId(schedulingBooking.getVendorDriverId());
                        driverDTO.setDriverName(vendorDriver.getDriverName());
                        driverDTO.setContactNo(vendorDriver.getContactNo());
                        driverDTO.setAltContactNo(vendorDriver.getAltContactNo());

                        schedulingBookingDTO.setVendorDriver(driverDTO);

                        logger.info("Driver info set successfully for booking {} - Driver: {}, Phone: {}",
                                schedulingBooking.getId(),
                                vendorDriver.getDriverName(),
                                vendorDriver.getContactNo());
                    } else {
                        logger.warn("VendorDriver API returned null for ID: {}", schedulingBooking.getVendorDriverId());
                        schedulingBookingDTO.setVendorDriver(null);
                    }
                } catch (Exception e) {
                    logger.error("Error fetching vendorDriver for ID: {} - {}", schedulingBooking.getVendorDriverId(), e.getMessage());
                    schedulingBookingDTO.setVendorDriver(null);
                }
            } else {
                logger.info("No vendorDriverId found for booking {} (vendorDriverId: {})",
                        schedulingBooking.getId(), schedulingBooking.getVendorDriverId());
                schedulingBookingDTO.setVendorDriver(null);
            }
            try {
                String userServiceUrl = "https://api.worldtriplink.com/auth/getCarRentalUserById/" + userId;
                logger.info("Calling user API: {}", userServiceUrl);
                CarRentalUser user = restTemplate.getForObject(userServiceUrl, CarRentalUser.class);

                if (user != null) {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setId(user.getId());
                    userDTO.setUserName(user.getUserName());
                    userDTO.setLastName(user.getLastName());
                    userDTO.setEmail(user.getEmail());
                    userDTO.setGender(user.getGender());
                    userDTO.setPhone(user.getPhone());
                    schedulingBookingDTO.setUser(userDTO);
                }
            } catch (Exception e) {
                logger.error("Error fetching user for ID: {} - {}", userId, e.getMessage());
                schedulingBookingDTO.setUser(null);
            }

            // Handle Scheduled Dates
            List<ScheduleDateBookingDTO> scheduledDateDTOs = schedulingBooking.getScheduledDates().stream().map(sd -> {
                ScheduleDateBookingDTO sdDTO = new ScheduleDateBookingDTO();
                sdDTO.setId(sd.getId());
                sdDTO.setDate(sd.getDate());
                sdDTO.setSlotId(sd.getSlotId());
                sdDTO.setStatus(sd.getStatus()); // Make sure status is set
                return sdDTO;
            }).collect(Collectors.toList());
            schedulingBookingDTO.setScheduledDates(scheduledDateDTOs);

            dtoList.add(schedulingBookingDTO);
        }

        return dtoList;
    }

    public List<SchedulingBookingDTO> getBookingByVendorDriverId(int vendorDriverId) {

        List<SchedulingBooking> bookings = this.scheduleBookingRepository.findByVendorDriverId(vendorDriverId);
        List<SchedulingBookingDTO> dtoList = new ArrayList<>();

        for (SchedulingBooking schedulingBooking : bookings) {
            SchedulingBookingDTO schedulingBookingDTO = new SchedulingBookingDTO();
            schedulingBookingDTO.setId(schedulingBooking.getId());
            schedulingBookingDTO.setPickUpLocation(schedulingBooking.getPickUpLocation());
            schedulingBookingDTO.setDropLocation(schedulingBooking.getDropLocation());
            schedulingBookingDTO.setTime(schedulingBooking.getTime());
            schedulingBookingDTO.setReturnTime(schedulingBooking.getReturnTime());
            schedulingBookingDTO.setShiftTime(schedulingBooking.getShiftTime());
            schedulingBookingDTO.setBookingType(schedulingBooking.getBookingType());

            try {
                String vendorServiceUrl = "https://api.worldtriplink.com/vendors/" + schedulingBooking.getVendorId();
                Vendor vendor = restTemplate.getForObject(vendorServiceUrl, Vendor.class);
                if (vendor != null) {
                    VendorDTO vendorDTO = new VendorDTO();
                    vendorDTO.setId(vendor.getId());
                    vendorDTO.setVendorCompanyName(vendor.getVendorCompanyName());
                    vendorDTO.setContactNo(vendor.getContactNo());
                    vendorDTO.setAlternateMobileNo(vendor.getAlternateMobileNo());
                    vendorDTO.setCity(vendor.getCity());
                    vendorDTO.setVendorEmail(vendor.getVendorEmail());
                    schedulingBookingDTO.setVendor(vendorDTO);
                }
            } catch (Exception e) {
                System.out.println("Vendor not found for ID: " + schedulingBooking.getVendorId());
                schedulingBookingDTO.setVendor(null);
            }

            try {
                String vendorDriverServiceUrl = "https://api.worldtriplink.com/vendorDriver/" + schedulingBooking.getVendorDriverId();
                VendorDriver vendorDriver = restTemplate.getForObject(vendorDriverServiceUrl, VendorDriver.class);
                if (vendorDriver != null) {
                    VendorDriverDTO driverDTO = new VendorDriverDTO();
                    driverDTO.setVendorDriverId(vendorDriver.getVendorDriverId());
                    driverDTO.setDriverName(vendorDriver.getDriverName());
                    driverDTO.setContactNo(vendorDriver.getContactNo());
                    driverDTO.setAltContactNo(vendorDriver.getAltContactNo());
                    schedulingBookingDTO.setVendorDriver(driverDTO);
                }
            } catch (Exception e) {
                System.out.println("VendorDriver not found for ID: " + schedulingBooking.getVendorDriverId());
                schedulingBookingDTO.setVendorDriver(null);
            }

            try {
                String userServiceUrl = "https://api.worldtriplink.com/auth/getCarRentalUserById/" + schedulingBooking.getUser().getId();
                CarRentalUser user = restTemplate.getForObject(userServiceUrl, CarRentalUser.class);
                if (user != null) {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setId(user.getId());
                    userDTO.setUserName(user.getUserName());
                    userDTO.setLastName(user.getLastName());
                    userDTO.setEmail(user.getEmail());
                    userDTO.setGender(user.getGender());
                    userDTO.setPhone(user.getPhone());
                    schedulingBookingDTO.setUser(userDTO);
                }
            } catch (Exception e) {
                System.out.println("User not found for ID: " + schedulingBooking.getUser().getId());
                schedulingBookingDTO.setUser(null);
            }

            List<ScheduleDateBookingDTO> scheduledDateDTOs = schedulingBooking.getScheduledDates().stream().map(sd -> {
                ScheduleDateBookingDTO sdDTO = new ScheduleDateBookingDTO();
                sdDTO.setId(sd.getId());
                sdDTO.setDate(sd.getDate());
                return sdDTO;
            }).toList();
            schedulingBookingDTO.setScheduledDates(scheduledDateDTOs);

            dtoList.add(schedulingBookingDTO);
        }

        return dtoList;
    }

    public ScheduledDate updateStatusByUserIdAndDate(int userId, LocalDate backendDate) {
        LocalDate currentDate = LocalDate.now();

        if (!currentDate.isEqual(backendDate)) {
            return null;
        }

        List<SchedulingBooking> bookings = scheduleBookingRepository.findByCarRentalUserId(userId);

        for (SchedulingBooking booking : bookings) {
            for (ScheduledDate schedulingDate : booking.getScheduledDates()) {
                if (schedulingDate.getDate().isEqual(currentDate)) {
                    schedulingDate.setStatus("COMPLETED");
                    return scheduleDates.save(schedulingDate);
                }
            }
        }

        return null;
    }


    @Autowired
    private LocationValidationService locationValidationService;

    public LocationValidationResult validatePickupAndDropLocations(String pickUpLocation, String dropLocation) {
        logger.info("🔍 Validating locations - Pickup: '{}', Drop: '{}'", pickUpLocation, dropLocation);

        try {
            // Basic input validation
            if (pickUpLocation == null || pickUpLocation.trim().isEmpty()) {
                return LocationValidationResult.error(
                        "MISSING_PICKUP_LOCATION",
                        "Pickup location is required"
                );
            }

            if (dropLocation == null || dropLocation.trim().isEmpty()) {
                return LocationValidationResult.error(
                        "MISSING_DROP_LOCATION",
                        "Drop location is required"
                );
            }

            // Enhanced Pune District validation
            boolean isPickupValid = isValidPuneDistrictLocation(pickUpLocation.trim());
            boolean isDropValid = isValidPuneDistrictLocation(dropLocation.trim());

            if (!isPickupValid) {
                return LocationValidationResult.error(
                        "PICKUP_LOCATION_OUT_OF_PUNE_DISTRICT",
                        "Pickup location is outside Pune District service area. Please select a pickup location within Pune District.",
                        Arrays.asList(
                                "Ensure location is within Pune District (includes Pune city, Shirur, Baramati, Maval, etc.)",
                                "Include 'Pune' or area name (e.g., 'Kharadi, Pune' or 'Shirur, Maharashtra')",
                                "Use complete address with area/town name",
                                "Supported: Pune city, PCMC, Shirur, Baramati, Lonavala, Chakan"
                        )
                );
            }

            if (!isDropValid) {
                return LocationValidationResult.error(
                        "DROP_LOCATION_OUT_OF_PUNE_DISTRICT",
                        "Drop location is outside Pune District service area. Please select a drop location within Pune District.",
                        Arrays.asList(
                                "Ensure location is within Pune District (includes Pune city, Shirur, Baramati, Maval, etc.)",
                                "Include 'Pune' or area name (e.g., 'Hadapsar, Pune' or 'Baramati, Maharashtra')",
                                "Use complete address with area/town name",
                                "Supported: Pune city, PCMC, Shirur, Baramati, Lonavala, Chakan"
                        )
                );
            }

            // Additional distance validation
            double tripDistance = getTripDistanceKm(pickUpLocation, dropLocation);
            if (tripDistance > 0 && tripDistance < 1) {
                return LocationValidationResult.error(
                        "TRIP_TOO_SHORT",
                        "Trip distance too short. Minimum trip distance is 1 km.",
                        Arrays.asList(
                                "Choose locations that are at least 1 km apart",
                                "Verify your addresses are correct"
                        )
                );
            }

            // Smart distance validation based on route type
            if (tripDistance > 160.0) {
                return LocationValidationResult.error(
                        "TRIP_DISTANCE_EXCEEDS_DISTRICT_LIMIT",
                        String.format("Trip distance (%.1fkm) exceeds Pune District service limit (120km max). Please ensure both locations are within Pune District.", tripDistance),
                        Arrays.asList(
                                "Pune city routes: up to 50km",
                                "Pune District intercity: up to 120km (e.g., Pune to Shirur ~40km, Pune to Baramati ~70km)",
                                "Verify both locations are within Pune District boundaries"
                        )
                );
            }

            logger.info("✅ Location validation successful - Pickup: '{}', Drop: '{}', Distance: {}km",
                    pickUpLocation, dropLocation, tripDistance);

            return LocationValidationResult.success("Both locations validated within Pune District service area");

        } catch (Exception e) {
            logger.error("Error during location validation: {}", e.getMessage(), e);
            return LocationValidationResult.error(
                    "VALIDATION_SYSTEM_ERROR",
                    "Location validation service temporarily unavailable. Please try again."
            );
        }
    }

    /**
     * ADD this new method to your ScheduleBookingService.java:
     */
    private boolean isValidPuneDistrictLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return false;
        }

        String locationLower = location.toLowerCase();
        logger.debug("🔍 Checking Pune District validity for: '{}'", location);

        // Strategy 1: Pune District postal codes
        String[] puneDistrictPostalCodes = {
                // Pune City (411xxx)
                "411001", "411002", "411003", "411004", "411005", "411006", "411007", "411008",
                "411009", "411010", "411011", "411012", "411013", "411014", "411015", "411016",
                "411017", "411018", "411019", "411020", "411021", "411022", "411023", "411024",
                "411025", "411026", "411027", "411028", "411029", "411030", "411031", "411032",
                "411033", "411034", "411035", "411036", "411037", "411038", "411039", "411040",
                "411041", "411042", "411043", "411044", "411045", "411046", "411047", "411048",
                "411051", "411052", "411057", "411061", "411062",

                // Shirur area (412xxx)
                "412208", "412210", "412211", "412212", "412213", "412214", "412215", "412216",

                // Baramati area (413xxx)
                "413102", "413103", "413133", "413134", "413135", "413136", "413137", "413138",

                // Maval area (410xxx)
                "410401", "410403", "410405", "410406", "410407", "410501", "410502", "410506"
        };

        for (String postalCode : puneDistrictPostalCodes) {
            if (locationLower.contains(postalCode)) {
                logger.debug("✅ Found Pune District postal code: {}", postalCode);
                return true;
            }
        }

        // Strategy 2: Known Pune District areas
        String[] puneDistrictAreas = {
                // Pune City + PCMC
                "pune", "pimpri", "chinchwad", "akurdi", "nigdi", "pradhikaran", "bhosari",
                "deccan", "shivajinagar", "camp", "koregaon park", "kothrud", "baner", "aundh",
                "kharadi", "hadapsar", "magarpatta", "hinjewadi", "wakad", "viman nagar",
                "katraj", "kondhwa", "undri", "pisoli", "wagholi", "mundhwa",

                // District towns
                "shirur", "baramati", "maval", "mulshi", "velhe", "bhor", "purandar",
                "daund", "indapur", "khed", "ambegaon", "junnar", "rajgurunagar",
                "lonavala", "khandala", "talegaon", "chakan", "alandi", "dehu"
        };

        for (String area : puneDistrictAreas) {
            if (locationLower.contains(area)) {
                logger.debug("✅ Found Pune District area: {}", area);
                return true;
            }
        }

        // Strategy 3: Coordinate validation (if available)
        try {
            double[] coordinates = getCoordinates(location);
            if (coordinates != null) {
                double lat = coordinates[0];
                double lng = coordinates[1];

                // Pune District bounds (expanded to include Shirur, Baramati, etc.)
                boolean inPuneDistrict = lat >= 18.0000 && lat <= 19.1000 &&
                        lng >= 73.2000 && lng <= 74.8000;

                if (inPuneDistrict) {
                    logger.debug("✅ Location within Pune District coordinates: lat={}, lng={}", lat, lng);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.debug("Could not get coordinates for validation: {}", e.getMessage());
        }

        // Strategy 4: Keywords (fallback)
        if (locationLower.contains("maharashtra") &&
                (locationLower.contains("pune") || locationLower.contains("411") ||
                        locationLower.contains("412") || locationLower.contains("413") ||
                        locationLower.contains("410"))) {
            logger.debug("✅ Found Maharashtra + Pune District indicators");
            return true;
        }

        logger.debug("❌ Location not recognized as Pune District");
        return false;
    }
    /**
     * Validates location by checking distance from Pune center
     */
    private LocationValidationResult validateLocationByDistance(String location, String locationType) {
        try {
            // Get coordinates using your existing method
            double[] coordinates = getCoordinates(location);

            if (coordinates == null) {
                return LocationValidationResult.error("GEOCODING_FAILED",
                        "Unable to find " + locationType + " location. Please provide a more specific address.");
            }

            // Pune center coordinates (Deccan area)
            double PUNE_CENTER_LAT = 18.5204;
            double PUNE_CENTER_LNG = 73.8567;

            // Calculate distance from Pune center
            double distanceFromPuneKm = calculateDistance(
                    coordinates[0], coordinates[1], PUNE_CENTER_LAT, PUNE_CENTER_LNG);

            // Service radius: 80km covers entire Pune District (Shirur, Baramati, etc.)
            double PUNE_DISTRICT_RADIUS_KM = 80.0;

            if (distanceFromPuneKm <= PUNE_DISTRICT_RADIUS_KM) {
                logger.info("✅ {} location within {}km of Pune center", locationType, Math.round(distanceFromPuneKm));
                return LocationValidationResult.success("Location validated within Pune District area");
            } else {
                logger.warn("❌ {} location {}km from Pune center (limit: {}km)",
                        locationType, Math.round(distanceFromPuneKm), PUNE_DISTRICT_RADIUS_KM);
                return LocationValidationResult.error(
                        locationType.toUpperCase() + "_LOCATION_OUT_OF_PUNE",
                        String.format("%s location is %.0fkm from Pune (service limit: %.0fkm). Please select a location within Pune District.",
                                locationType, distanceFromPuneKm, PUNE_DISTRICT_RADIUS_KM));
            }

        } catch (Exception e) {
            logger.error("Error validating {} location: {}", locationType, e.getMessage());
            return LocationValidationResult.error("DISTANCE_VALIDATION_ERROR", "Error validating location distance");
        }
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Earth radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in kilometers
    }
    private double getTripDistanceKm(String origin, String destination) {
        try {
            double distanceInMeters = getDirectRouteDistance(origin, destination);
            return distanceInMeters / 1000;
        } catch (Exception e) {
            logger.error("Error calculating trip distance: {}", e.getMessage());
            return -1;
        }
    }


    public Map<String, Object> getLocationServiceHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            boolean isHealthy = locationValidationService.isServiceHealthy();
            Map<String, Object> cacheStats = locationValidationService.getCacheStats();

            health.put("status", isHealthy ? "UP" : "DOWN");
            health.put("cacheStats", cacheStats);
            health.put("lastChecked", LocalDateTime.now());

            // Test validation with known location
            LocationValidationResult testResult = locationValidationService.validateLocation("Deccan, Pune", "test");
            health.put("testValidation", testResult.isValid() ? "PASS" : "FAIL");

        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }

        return health;
    }

    /**
     * Clear location cache (for maintenance)
     */
    public void clearLocationCache() {
        locationValidationService.clearCache();
        logger.info("Location validation cache cleared");
    }
}