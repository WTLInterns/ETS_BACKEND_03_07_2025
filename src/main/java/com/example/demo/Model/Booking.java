package com.example.demo.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Entity representing a booking in the system.
 * This class is used by ScheduleBookingService and SchedulingBookingController.
 */
@Entity
@Table(name = "bookings")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String bookId;
    private Integer userId;
    private String pickUpLocation;
    private String dropLocation;
    private String cabType;
    private Double distance;
    private Double fare;
    private String finalAmount;
    private String baseAmount;
    private String serviceCharge;
    private String gst;
    private String time;
    private String returnTime;
    private String shiftTime;
    private LocalDate bookingDate;
    private LocalDateTime bookingTime;
    private String status; // PENDING, CONFIRMED, CANCELLED
    private String paymentStatus; // PENDING, COMPLETED
    private Long vendorId;
    private Integer vendorDriverId;
    
    // Default constructor
    public Booking() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getBookId() {
        return bookId;
    }
    
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getPickUpLocation() {
        return pickUpLocation;
    }
    
    public void setPickUpLocation(String pickUpLocation) {
        this.pickUpLocation = pickUpLocation;
    }
    
    public String getDropLocation() {
        return dropLocation;
    }
    
    public void setDropLocation(String dropLocation) {
        this.dropLocation = dropLocation;
    }
    
    public String getCabType() {
        return cabType;
    }
    
    public void setCabType(String cabType) {
        this.cabType = cabType;
    }
    
    public Double getDistance() {
        return distance;
    }
    
    public void setDistance(Double distance) {
        this.distance = distance;
    }
    
    public Double getFare() {
        return fare;
    }
    
    public void setFare(Double fare) {
        this.fare = fare;
    }
    
    public String getFinalAmount() {
        return finalAmount;
    }
    
    public void setFinalAmount(String finalAmount) {
        this.finalAmount = finalAmount;
    }
    
    public String getBaseAmount() {
        return baseAmount;
    }
    
    public void setBaseAmount(String baseAmount) {
        this.baseAmount = baseAmount;
    }
    
    public String getServiceCharge() {
        return serviceCharge;
    }
    
    public void setServiceCharge(String serviceCharge) {
        this.serviceCharge = serviceCharge;
    }
    
    public String getGst() {
        return gst;
    }
    
    public void setGst(String gst) {
        this.gst = gst;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public String getReturnTime() {
        return returnTime;
    }
    
    public void setReturnTime(String returnTime) {
        this.returnTime = returnTime;
    }
    
    public String getShiftTime() {
        return shiftTime;
    }
    
    public void setShiftTime(String shiftTime) {
        this.shiftTime = shiftTime;
    }
    
    public LocalDate getBookingDate() {
        return bookingDate;
    }
    
    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }
    
    public LocalDateTime getBookingTime() {
        return bookingTime;
    }
    
    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public Long getVendorId() {
        return vendorId;
    }
    
    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }
    
    public Integer getVendorDriverId() {
        return vendorDriverId;
    }
    
    public void setVendorDriverId(Integer vendorDriverId) {
        this.vendorDriverId = vendorDriverId;
    }
    
    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", bookId='" + bookId + '\'' +
                ", userId=" + userId +
                ", pickUpLocation='" + pickUpLocation + '\'' +
                ", dropLocation='" + dropLocation + '\'' +
                ", cabType='" + cabType + '\'' +
                ", distance=" + distance +
                ", fare=" + fare +
                ", finalAmount='" + finalAmount + '\'' +
                ", baseAmount='" + baseAmount + '\'' +
                ", serviceCharge='" + serviceCharge + '\'' +
                ", gst='" + gst + '\'' +
                ", time='" + time + '\'' +
                ", returnTime='" + returnTime + '\'' +
                ", shiftTime='" + shiftTime + '\'' +
                ", bookingDate=" + bookingDate +
                ", bookingTime=" + bookingTime +
                ", status='" + status + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", vendorId=" + vendorId +
                ", vendorDriverId=" + vendorDriverId +
                '}';
    }
}
