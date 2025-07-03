package com.example.demo.Model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.annotation.Generated;

@Entity
public class SchedulingBooking {



    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String pickUpLocation;

    private String bookId;

    private String dropLocation;

    private String time;

    private String returnTime;

    private String cabType;

    private Long vendorId;

    private int vendorDriverId;

    @Transient
    private Vendor vendor;

    private String baseAmount;

    private String finalAmount;

    private String serviceCharge;

    private String gst;

    private String distance;

    private int sittingExcepatation;

    @Transient
    private VendorDriver vendorDriver;

    private int partnerSharing;

    private String shiftTime;

    @ElementCollection
    private List<LocalDate> dateOfList;

    private String bookingType="SCHEDULE";

    private String status;

    private String slotId;



    @Transient
    private CarRentalUser carRentaluser;

    private int carRentalUserId;


    @OneToMany(mappedBy = "schedulingBooking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ScheduledDate> scheduledDates = new ArrayList<>();



    public SchedulingBooking(int id, String pickUpLocation, String dropLocation, String time, String returnTime,
                             Long vendorId, int vendorDriverId, Vendor vendor, String baseAmount, String finalAmount,
                             String serviceCharge, String gst, VendorDriver vendorDriver, String shiftTime, List<LocalDate> dateOfList,
                             String bookingType, CarRentalUser carRentaluser, List<ScheduledDate> scheduledDates, String cabType, String distance, int sittingExcepatation, String bookId, int partnerSharing, String status, int carRentalUserId, String slotId ) {
        this.id = id;
        this.pickUpLocation = pickUpLocation;
        this.dropLocation = dropLocation;
        this.status=status;
        this.time = time;
        this.returnTime = returnTime;
        this.vendorId = vendorId;
        this.vendorDriverId = vendorDriverId;
        this.vendor = vendor;
        this.baseAmount = baseAmount;
        this.finalAmount = finalAmount;
        this.serviceCharge = serviceCharge;
        this.gst = gst;
        this.vendorDriver = vendorDriver;
        this.shiftTime = shiftTime;
        this.dateOfList = dateOfList;
        this.bookingType = bookingType;
        this.carRentaluser=carRentaluser;
        this.scheduledDates = scheduledDates;
        this.cabType=cabType;
        this.distance=distance;
        this.sittingExcepatation=sittingExcepatation;
        this.bookId=bookId;
        this.partnerSharing=partnerSharing;
        this.slotId=slotId;
    }

    public SchedulingBooking(){
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

    public List<LocalDate> getDateOfList() {
        return dateOfList;
    }

    public void setDateOfList(List<LocalDate> dateOfList) {
        this.dateOfList = dateOfList;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public CarRentalUser getUser() {
        return carRentaluser;
    }

    public void setUser(CarRentalUser carRentalUser) {
        this.carRentaluser = carRentalUser;
    }

    public List<ScheduledDate> getScheduledDates() {
        return scheduledDates;
    }

    public void setScheduledDates(List<ScheduledDate> scheduledDates) {
        this.scheduledDates = scheduledDates;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public int getVendorDriverId() {
        return vendorDriverId;
    }

    public void setVendorDriverId(int vendorDriverId) {
        this.vendorDriverId = vendorDriverId;
    }

    public VendorDriver getVendorDriver() {
        return vendorDriver;
    }

    public void setVendorDriver(VendorDriver vendorDriver) {
        this.vendorDriver = vendorDriver;
    }

    public String getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(String baseAmount) {
        this.baseAmount = baseAmount;
    }

    public String getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(String finalAmount) {
        this.finalAmount = finalAmount;
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

    public String getCabType() {
        return cabType;
    }

    public void setCabType(String cabType) {
        this.cabType = cabType;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public int getSittingExcepatation() {
        return sittingExcepatation;
    }

    public void setSittingExcepatation(int sittingExcepatation) {
        this.sittingExcepatation = sittingExcepatation;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getPartnerSharing() {
        return partnerSharing;
    }

    public void setPartnerSharing(int partnerSharing) {
        this.partnerSharing = partnerSharing;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCarRentalUserId() {
        return carRentalUserId;
    }

    public void setCarRentalUserId(int carRentalUserId) {
        this.carRentalUserId = carRentalUserId;
    }



    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public CarRentalUser getCarRentaluser() {
        return carRentaluser;
    }

    public void setCarRentaluser(CarRentalUser carRentaluser) {
        this.carRentaluser = carRentaluser;
    }

    @Override
    public String toString() {
        return "SchedulingBooking [id=" + id + ", pickUpLocation=" + pickUpLocation + ", bookId=" + bookId
                + ", dropLocation=" + dropLocation + ", time=" + time + ", returnTime=" + returnTime + ", cabType="
                + cabType + ", vendorId=" + vendorId + ", vendorDriverId=" + vendorDriverId + ", vendor=" + vendor
                + ", baseAmount=" + baseAmount + ", finalAmount=" + finalAmount + ", serviceCharge=" + serviceCharge
                + ", gst=" + gst + ", distance=" + distance + ", sittingExcepatation=" + sittingExcepatation
                + ", vendorDriver=" + vendorDriver + ", partnerSharing=" + partnerSharing + ", shiftTime=" + shiftTime
                + ", dateOfList=" + dateOfList + ", bookingType=" + bookingType + ", status=" + status
                + ", carRentaluser=" + carRentaluser + ", carRentalUserId=" + carRentalUserId + ", scheduledDates="
                + scheduledDates + "]";
    }
}
