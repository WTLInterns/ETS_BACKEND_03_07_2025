package com.example.demo.DTO;

public class SlotBookingDTO {
    private int userId;
    private int vendorId;
    private int vendorDriverId;
    private int bookingId;
    private String pickupTime;
    private String pickupLocation;
    private String dropLocation;
    private String status;
    private String userName;
    private String phone;

    public SlotBookingDTO() {}

    public SlotBookingDTO(int userId, int vendorId, int vendorDriverId, int bookingId,
                          String pickupTime, String pickupLocation, String dropLocation, String status,String userName,String phone) {
        this.userId = userId;
        this.vendorId = vendorId;
        this.vendorDriverId = vendorDriverId;
        this.bookingId = bookingId;
        this.pickupTime = pickupTime;
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.status = status;
        this.userName=userName;
        this.phone=phone;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public int getVendorDriverId() {
        return vendorDriverId;
    }

    public void setVendorDriverId(int vendorDriverId) {
        this.vendorDriverId = vendorDriverId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(String pickupTime) {
        this.pickupTime = pickupTime;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
         this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

