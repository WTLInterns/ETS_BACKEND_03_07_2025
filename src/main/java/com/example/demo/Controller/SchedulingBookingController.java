package com.example.demo.Controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import com.example.demo.DTO.DriverSlotsResponseDTO;
import com.example.demo.DTO.MultiDateAssignmentResponseDTO;
import com.example.demo.Service.Location.LocationValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.demo.DTO.SchedulingBookingDTO;
import com.example.demo.Model.Price;
import com.example.demo.Model.ScheduledDate;
import com.example.demo.Model.SchedulingBooking;
import com.example.demo.Model.CarRentalUser;
import com.example.demo.Repository.ScheduleBookingRepository;
import com.example.demo.Service.EmailService;
import com.example.demo.Service.GeocodingService;
import com.example.demo.Service.PriceService;
import com.example.demo.Service.ScheduleBookingService;

@RestController
@RequestMapping("/schedule")
public class SchedulingBookingController {

    @Autowired
    private ScheduleBookingService scheduleBookingService;



    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private PriceService priceService;


    @Autowired
    private ScheduleBookingRepository scheduleBookingRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RestTemplate restTemplate;

    // @PostMapping("/confirmBooking")
    // public ResponseEntity<SchedulingBooking> createScheduleBooking(
    // @RequestParam int userId,
    // @RequestParam String pickUpLocation,
    // @RequestParam String dropLocation,
    // @RequestParam String time,
    // @RequestParam String returnTime,
    // @RequestParam String shiftTime,
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate>
    // dates
    // ) {
    // SchedulingBooking booking = scheduleBookingService.createSchedule(
    // userId,
    // pickUpLocation,
    // dropLocation,
    // time,
    // returnTime,
    // shiftTime,
    // dates
    // );

    // return ResponseEntity.ok(booking);
    // }

    // public SchedulingBooking confirmBooking(@RequestParam int userId,
    // @RequestParam String pickupLocation,
    // @RequestParam String dropLocation, @RequestParam String time, @RequestParam
    // String returnTime,
    // @RequestParam String cabType, @RequestParam String baseAmount, @RequestParam
    // String finalAmount,
    // @RequestParam String gst, String serviceCharge) {

    // }

    @PutMapping("/{bookingId}/assign-vendor/{vendorId}")
    public ResponseEntity<SchedulingBooking> assignVendorToBooking(
            @PathVariable int bookingId,
            @PathVariable Long vendorId) {

        SchedulingBooking updatedBooking = scheduleBookingService.assignVendorToBooking(bookingId, vendorId);
        return ResponseEntity.ok(updatedBooking);
    }

    @PutMapping("/{bookingId}/assignDriver/{vendorDriverId}")
    public ResponseEntity<MultiDateAssignmentResponseDTO> assignVendorDriverWithResponse(
            @PathVariable int bookingId,
            @PathVariable int vendorDriverId) {

        MultiDateAssignmentResponseDTO response = scheduleBookingService.assignDriverBookingWithResponse(bookingId, vendorDriverId);

        if (response.isOverallSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/byUserId/{id}")
    public List<SchedulingBookingDTO> getBookingByUserId(@PathVariable int id) {
        return this.scheduleBookingService.findByUserId(id);

    }

    @GetMapping("/byUser/{id}")
    public List<SchedulingBookingDTO> getByUserId(@PathVariable int id) {
        return this.scheduleBookingService.findByUserId(id);
    }

    @GetMapping("/byVendorDriverId/{vendorDriverId}")
    public List<SchedulingBookingDTO> getByVendorDriverId(@PathVariable int vendorDriverId) {
        return this.scheduleBookingService.getBookingByVendorDriverId(vendorDriverId);
    }

    @PutMapping("/update-status/{userId}")
    public ResponseEntity<?> updateStatus(
            @PathVariable int userId,
            @RequestParam String date) {

        try {
            LocalDate localDate = LocalDate.parse(date);
            ScheduledDate updatedDate = scheduleBookingService.updateStatusByUserIdAndDate(userId, localDate);

            if (updatedDate != null) {
                return ResponseEntity.ok(updatedDate);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No booking found with the given date for this user or date mismatch.");
            }
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Expected yyyy-MM-dd.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while updating status: " + e.getMessage());
        }
    }

    @GetMapping("/driver/{driverId}/slots")
    public ResponseEntity<DriverSlotsResponseDTO> getDriverSlots(@PathVariable int driverId) {
        try {
            DriverSlotsResponseDTO response = scheduleBookingService.getDriverSlots(driverId);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(new DriverSlotsResponseDTO(new ArrayList<>()));

        } catch (Exception e) {
//            logger.error("Error in getDriverSlots: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DriverSlotsResponseDTO(new ArrayList<>()));
        } 
    }

    @PostMapping("/etsCab1")
        public Map<String, Object> getCabChoose(@RequestParam String pickUpLocation, @RequestParam String dropLocation,
                                            @RequestParam String time, @RequestParam(required = false) String returnTime, @RequestParam String shiftTime, @RequestParam List<LocalDate> dates) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Use production-ready location validation
            LocationValidationResult locationResult = scheduleBookingService.validatePickupAndDropLocations(
                    pickUpLocation,
                    dropLocation
            );

            if (!locationResult.isValid()) {
                response.put("status", "error");
                response.put("error", locationResult.getErrorMessage());
                response.put("errorCode", locationResult.getErrorCode());

                if (locationResult.getSuggestions() != null && !locationResult.getSuggestions().isEmpty()) {
                    response.put("suggestions", locationResult.getSuggestions());
                }

                // Add debugging info for development
                response.put("validationMethod", locationResult.getValidationMethod());

                return response;
            }


            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            try {
                LocalTime userTime = LocalTime.parse(time, timeFormatter);
                LocalDateTime bookingTime = LocalDateTime.of(LocalDate.now(), userTime);

                if (bookingTime.isBefore(LocalDateTime.now())) {
                    bookingTime = bookingTime.plusDays(1);
                }

                Duration duration = Duration.between(LocalDateTime.now(), bookingTime);
                long hoursDiff = duration.toHours();

                if (hoursDiff < 3) {
                    response.put("error", "Booking must be made at least 3 hours in advance.");
                    return response;
                }
            } catch (DateTimeParseException e) {
                response.put("error", "Invalid time format. Please use HH:mm format (e.g., 14:30)");
                return response;
            }

            Price price = this.priceService.findPriceByPickupAndDrop(pickUpLocation, dropLocation);
            System.out.println(price);

            response.put("pickUpLocation", pickUpLocation);
            response.put("dropLocation", dropLocation);
            response.put("time", time);
            response.put("returnTime", returnTime);
            response.put("shiftTime", shiftTime);
            response.put("destinationState", price.getDesitnationState());
            response.put("destinationCity", price.getDestinationCity());
            response.put("sourceState", price.getSourceState());
            response.put("sourceCity", price.getSourceCity());
            response.put("hatchback", price.getHatchback());
            response.put("sedan", price.getSedan());
            response.put("suv", price.getSuv());
            response.put("distance", price.getDistance());
            response.put("dates", dates);
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", "Service temporarily unavailable. Please try again.");
            response.put("errorCode", "SYSTEM_ERROR");
            return response;
        }
    }


    @PostMapping("/cabFinder")
    public Map<String, Object> findCabOverDetails(@RequestParam String pickUpLocation,
                                                  @RequestParam String dropLocation,
                                                  @RequestParam String time,
                                                  @RequestParam String returnTime,
                                                  @RequestParam String shiftTime,
                                                  @RequestParam String distance,
                                                  @RequestParam String hatchback,
                                                  @RequestParam String sedan,
                                                  @RequestParam String suv) {

        Map<String, Object> response = new HashMap<>();

        double totalDistance = Double.parseDouble(distance);
        double baseFare = 220.0;




        double hatchbackRate = Double.parseDouble(hatchback);
        double sedanRate = Double.parseDouble(sedan);
        double suvRate = Double.parseDouble(suv);

        double hatchbackFare;
        double sedanFare;
        double suvFare;

        if (totalDistance <= 5) {
            hatchbackFare = baseFare;
            sedanFare = baseFare;
            suvFare = baseFare;
        } else {
            double remainingDistance = totalDistance - 5;

            hatchbackFare = baseFare + (remainingDistance * hatchbackRate);
            sedanFare = baseFare + (remainingDistance * sedanRate);
            suvFare = baseFare + (remainingDistance * suvRate);
        }

        response.put("pickUpLocation", pickUpLocation);
        response.put("dropLocation", dropLocation);
        response.put("distance", totalDistance);
        response.put("time", time);
        response.put("returnTime", returnTime);
        response.put("shiftTime", shiftTime);

        response.put("hatchbackRate", hatchbackRate);
        response.put("sedanRate", sedanRate);
        response.put("suvRate", suvRate);

        response.put("hatchbackFare", hatchbackFare);
        response.put("sedanFare", sedanFare);
        response.put("suvFare", suvFare);

        return response;
    }

    @PostMapping("/invoice")
    public Map<String, Object> getInvoice(@RequestParam String baseFare, @RequestParam String cabType) {
        Map<String, Object> response = new HashMap<>();

        double fare = Double.parseDouble(baseFare);
        double gst = fare * 5 / 100;
        double serviceCharge = fare * 10 / 100;
        double totalAmount = fare + gst + serviceCharge;

        response.put("baseFare", fare);
        response.put("cabType", cabType);
        response.put("gst", gst);
        response.put("serviceCharge", serviceCharge);
        response.put("totalAmount", totalAmount);

        return response;
    }



    @PostMapping("/etsBookingConfirm")
    public Map<String, Object> confirmBooking(
            @RequestParam(required = false) String pickUpLocation,
            @RequestParam(required = false) String dropLocation,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String returnTime,
            @RequestParam(required = false) String cabType,
            @RequestParam(required = false) String finalAmount,
            @RequestParam(required = false) String baseAmount,
            @RequestParam(required = false) String serviceCharge,
            @RequestParam(required = false) String gst,
            @RequestParam(required = false) String distance,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") List<LocalDate> dates,
            @RequestParam(required = false) Integer userId) {

        Map<String, Object> response = new HashMap<>();

//        ScheduleBookingService.LocationValidationResult locationResult =
//                scheduleBookingService.validatePickupAndDropLocations(pickUpLocation, dropLocation);
//
//        if (!locationResult.isValid()) {
//            response.put("status", "error");
//            response.put("message", locationResult.getErrorMessage());
//            response.put("errorCode", locationResult.getErrorCode());
//
//            if (locationResult.getSuggestions() != null && !locationResult.getSuggestions().isEmpty()) {
//                response.put("suggestions", locationResult.getSuggestions());
//            }
//
//            return response;
//        }

        if (userId == null) {
            response.put("status", "error");
            response.put("message", "User ID is required");
            return response;
        }

        SchedulingBooking booking = new SchedulingBooking();
        String bookid = "ETS" + System.currentTimeMillis();
        booking.setBookId(bookid);

        booking.setPickUpLocation(pickUpLocation);
        booking.setDropLocation(dropLocation);
        booking.setTime(time);
        booking.setReturnTime(returnTime);
        booking.setCabType(cabType);
        booking.setFinalAmount(finalAmount);
        booking.setBaseAmount(baseAmount);
        booking.setServiceCharge(serviceCharge);
        booking.setGst(gst);
        booking.setDistance(distance);
        booking.setDateOfList(dates);
        booking.setBookingType("SCHEDULE");

        List<ScheduledDate> scheduledDateList = new ArrayList<>();
        for (LocalDate date : dates) {
            ScheduledDate sd = new ScheduledDate();
            sd.setDate(date);
            sd.setSchedulingBooking(booking);
            scheduledDateList.add(sd);
        }
        booking.setScheduledDates(scheduledDateList);

        String userServiceUrl = "https://api.worldtriplink.com/auth/getCarRentalUserById/" + userId;
        System.out.println("Calling URL: " + userServiceUrl);
        CarRentalUser user = restTemplate.getForObject(userServiceUrl, CarRentalUser.class);
        System.out.println("USER"+user);
        if (user == null) {
            System.out.println("RestTemplate returned null - service might be down or user not found");

            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        booking.setCarRentalUserId(userId);

        scheduleBookingRepository.save(booking);

        String subject = "Booking Confirmation - ETS";
        String message = buildBookingConfirmationEmail(booking);
        boolean emailSent = emailService.sendEmail(message, subject, user.getEmail());

        if (emailSent) {
            response.put("status", "success");
            response.put("message", "Booking confirmed successfully. Confirmation email sent.");
            response.put("bookingId", booking.getId());
        } else {
            response.put("status", "error");
            response.put("message", "Booking confirmed, but email sending failed.");
        }

        return response;
    }

    private String buildBookingConfirmationEmail(SchedulingBooking booking) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;'>")
                .append("<div style='background-color: #f8f9fa; padding: 20px; border-radius: 5px;'>")
                .append("<h2 style='color: #2c3e50; margin-top: 0;'>Booking Confirmation</h2>")
                // .append("<p>Dear " + booking.getUser().getUserName() + ",</p>")
                .append("<p style='margin-bottom: 20px;'>Your booking has been successfully confirmed. Here are your booking details:</p>")
                .append("<table style='width: 100%; border-collapse: collapse; margin-bottom: 20px; background-color: #fff;'>")
                .append("<tr style='background-color: #2c3e50; color: white;'>")
                .append("<th style='padding: 12px; text-align: left;'>Field</th>")
                .append("<th style='padding: 12px; text-align: left;'>Details</th>")
                .append("</tr>")
                .append("<tr style='border-bottom: 1px solid #ddd;'>")
                .append("<td style='padding: 12px; font-weight: bold;'>Booking ID</td>")
                .append("<td style='padding: 12px;'>" + booking.getBookId() + "</td>")
                .append("</tr>")
                .append("<tr style='border-bottom: 1px solid #ddd;'>")
                .append("<td style='padding: 12px; font-weight: bold;'>Pick-Up Location</td>")
                .append("<td style='padding: 12px;'>" + booking.getPickUpLocation() + "</td>")
                .append("</tr>")
                .append("<tr style='border-bottom: 1px solid #ddd;'>")
                .append("<td style='padding: 12px; font-weight: bold;'>Drop Location</td>")
                .append("<td style='padding: 12px;'>" + booking.getDropLocation() + "</td>")
                .append("</tr>")
                .append("<tr style='border-bottom: 1px solid #ddd;'>")
                .append("<td style='padding: 12px; font-weight: bold;'>Cab Type</td>")
                .append("<td style='padding: 12px;'>" + booking.getCabType() + "</td>")
                .append("</tr>")
                .append("<tr style='border-bottom: 1px solid #ddd;'>")
                .append("<td style='padding: 12px; font-weight: bold;'>Final Amount</td>")
                .append("<td style='padding: 12px;'>" + booking.getFinalAmount() + "</td>")
                .append("</tr>")
                .append("<tr style='border-bottom: 1px solid #ddd;'>")
                .append("<td style='padding: 12px; font-weight: bold;'>Base Amount</td>")
                .append("<td style='padding: 12px;'>" + booking.getBaseAmount() + "</td>")
                .append("</tr>")
                .append("<tr style='border-bottom: 1px solid #ddd;'>")
                .append("<td style='padding: 12px; font-weight: bold;'>Service Charge</td>")
                .append("<td style='padding: 12px;'>" + booking.getServiceCharge() + "</td>")
                .append("</tr>")
                .append("<tr style='border-bottom: 1px solid #ddd;'>")
                .append("<td style='padding: 12px; font-weight: bold;'>GST</td>")
                .append("<td style='padding: 12px;'>" + booking.getGst() + "</td>")
                .append("</tr>")
                .append("<tr style='border-bottom: 1px solid #ddd;'>")
                .append("<td style='padding: 12px; font-weight: bold;'>Distance</td>")
                .append("<td style='padding: 12px;'>" + booking.getDistance() + "</td>")
                .append("</tr>")
                .append("<tr style='border-bottom: 1px solid #ddd;'>")
                .append("<td style='padding: 12px; font-weight: bold;'>Sitting Expectation</td>")
                .append("<td style='padding: 12px;'>" + booking.getSittingExcepatation() + "</td>")
                .append("</tr>")
                .append("<tr>")
                .append("<td style='padding: 12px; font-weight: bold;'>Scheduled Dates</td>")
                .append("<td style='padding: 12px;'>" + booking.getDateOfList().toString() + "</td>")
                .append("</tr>")
                .append("</table>")
                .append("<p style='margin-bottom: 10px;'>Thank you for choosing our service!</p>")
                .append("<p style='margin-bottom: 0;'>Best regards,</p>")
                .append("<p style='margin-top: 0; font-weight: bold;'>WTL Tourism Pvt Ltd.</p>")
                .append("</div>")
                .append("</body></html>");
        return emailContent.toString();
    }





    @GetMapping("/getId/{id}")
    public SchedulingBookingDTO getById(@PathVariable int id){
        return this.scheduleBookingService.getByScheduleBookingId(id);
    }

    // @GetMapping("/coordinates")
    // public ResponseEntity<Map<String, Double>> getCoordinates(@RequestParam String address) {
    //     Map<String, Double> coords = geocodingService.getLatLngFromAddress(address);
    //     return ResponseEntity.ok(coords);
    // }



//     @PutMapping("/{bookingId}/assignVendorDriver/{vendorDriverId}")
//     public SchedulingBooking assignVendorDriver(@PathVariable int vendorDriverId, @PathVariable int bookingId){
// return this.scheduleBookingService.assignDriverBooking(bookingId, vendorDriverId);
//     }
}