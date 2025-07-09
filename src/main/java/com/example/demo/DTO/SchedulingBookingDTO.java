package com.example.demo.DTO;


import java.time.LocalDate;
import java.util.List;

import com.example.demo.Model.VendorCab;

public class SchedulingBookingDTO {
    private int id;
    private String pickUpLocation;
    private String dropLocation;
    private String time;
    private String returnTime;
    private String shiftTime;
    private String bookingType;
    private List<LocalDate> dateOfList;
    private String bookingId;

    private VendorDTO vendor;
    private VendorDriverDTO vendorDriver;
    private UserDTO user;
    private List<ScheduleDateBookingDTO> scheduledDates;

    private VendorCabDTO vendorCab;


    public SchedulingBookingDTO(int id, String pickUpLocation, String dropLocation, String time, String returnTime,
                                String shiftTime, String bookingType, List<LocalDate> dateOfList, VendorDTO vendor,
                                VendorDriverDTO vendorDriver, UserDTO user, List<ScheduleDateBookingDTO> scheduledDates, String bookingId,VendorCabDTO vendorCab) {
        this.id = id;
        this.pickUpLocation = pickUpLocation;
        this.dropLocation = dropLocation;
        this.vendorCab=vendorCab;
        this.time = time;
        this.returnTime = returnTime;
        this.shiftTime = shiftTime;
        this.bookingType = bookingType;
        this.dateOfList = dateOfList;
        this.vendor = vendor;
        this.vendorDriver = vendorDriver;
        this.user = user;
        this.scheduledDates = scheduledDates;
        this.bookingId=bookingId;
    }


    public SchedulingBookingDTO(){
        super();
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
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


    public String getBookingType() {
        return bookingType;
    }


    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }


    public List<LocalDate> getDateOfList() {
        return dateOfList;
    }


    public void setDateOfList(List<LocalDate> dateOfList) {
        this.dateOfList = dateOfList;
    }


    public VendorDTO getVendor() {
        return vendor;
    }


    public void setVendor(VendorDTO vendor) {
        this.vendor = vendor;
    }


    public VendorDriverDTO getVendorDriver() {
        return vendorDriver;
    }


    public void setVendorDriver(VendorDriverDTO vendorDriver) {
        this.vendorDriver = vendorDriver;
    }


    public UserDTO getUser() {
        return user;
    }


    public void setUser(UserDTO user) {
        this.user = user;
    }


    public List<ScheduleDateBookingDTO> getScheduledDates() {
        return scheduledDates;
    }


    public void setScheduledDates(List<ScheduleDateBookingDTO> scheduledDates) {
        this.scheduledDates = scheduledDates;
    }


    public String getBookingId() {
        return bookingId;
    }


    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }


    public VendorCabDTO getVendorCab() {
        return vendorCab;
    }


    public void setVendorCab(VendorCabDTO vendorCab) {
        this.vendorCab = vendorCab;
    }








}

    
