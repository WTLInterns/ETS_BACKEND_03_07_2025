package com.example.demo.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "drivers")
public class Driver {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    private String licenseNumber;
    private String phoneNumber;
    private String address;
    private String status; // Available, Busy, Offline
    private String documentUrl;
    
    // Additional fields based on the needed constructor
    private Long vendorId;
    private String driverPhotoUrl;
    private String licensePhotoFrontUrl;
    private String licensePhotoBackUrl;
    private String aadharPhotoFrontUrl;
    private String aadharPhotoBackUrl;
    private String panCardPhotoUrl;
    
    // Default constructor
    public Driver() {
    }
    
    // Parameterized constructor - original
    public Driver(String name, String licenseNumber, String phoneNumber, String address, 
                 String status, String documentUrl) {
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.status = status;
        this.documentUrl = documentUrl;
    }
    
    // Extended constructor required by DriverController
    public Driver(String name, Long vendorId, String licenseNumber, String phoneNumber, 
                 String address, Long id, String status, String driverPhotoUrl, 
                 String licensePhotoFrontUrl, String licensePhotoBackUrl, 
                 String aadharPhotoFrontUrl, String aadharPhotoBackUrl, String panCardPhotoUrl) {
        this.name = name;
        this.vendorId = vendorId;
        this.licenseNumber = licenseNumber;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.id = id.intValue(); // Convert Long to Integer
        this.status = status;
        this.driverPhotoUrl = driverPhotoUrl;
        this.licensePhotoFrontUrl = licensePhotoFrontUrl;
        this.licensePhotoBackUrl = licensePhotoBackUrl;
        this.aadharPhotoFrontUrl = aadharPhotoFrontUrl;
        this.aadharPhotoBackUrl = aadharPhotoBackUrl;
        this.panCardPhotoUrl = panCardPhotoUrl;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDocumentUrl() {
        return documentUrl;
    }
    
    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }
    
    public Long getVendorId() {
        return vendorId;
    }
    
    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }
    
    public String getDriverPhotoUrl() {
        return driverPhotoUrl;
    }
    
    public void setDriverPhotoUrl(String driverPhotoUrl) {
        this.driverPhotoUrl = driverPhotoUrl;
    }
    
    public String getLicensePhotoFrontUrl() {
        return licensePhotoFrontUrl;
    }
    
    public void setLicensePhotoFrontUrl(String licensePhotoFrontUrl) {
        this.licensePhotoFrontUrl = licensePhotoFrontUrl;
    }
    
    public String getLicensePhotoBackUrl() {
        return licensePhotoBackUrl;
    }
    
    public void setLicensePhotoBackUrl(String licensePhotoBackUrl) {
        this.licensePhotoBackUrl = licensePhotoBackUrl;
    }
    
    public String getAadharPhotoFrontUrl() {
        return aadharPhotoFrontUrl;
    }
    
    public void setAadharPhotoFrontUrl(String aadharPhotoFrontUrl) {
        this.aadharPhotoFrontUrl = aadharPhotoFrontUrl;
    }
    
    public String getAadharPhotoBackUrl() {
        return aadharPhotoBackUrl;
    }
    
    public void setAadharPhotoBackUrl(String aadharPhotoBackUrl) {
        this.aadharPhotoBackUrl = aadharPhotoBackUrl;
    }
    
    public String getPanCardPhotoUrl() {
        return panCardPhotoUrl;
    }
    
    public void setPanCardPhotoUrl(String panCardPhotoUrl) {
        this.panCardPhotoUrl = panCardPhotoUrl;
    }
    
    @Override
    public String toString() {
        return "Driver{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", status='" + status + '\'' +
                ", documentUrl='" + documentUrl + '\'' +
                ", vendorId=" + vendorId +
                ", driverPhotoUrl='" + driverPhotoUrl + '\'' +
                ", licensePhotoFrontUrl='" + licensePhotoFrontUrl + '\'' +
                ", licensePhotoBackUrl='" + licensePhotoBackUrl + '\'' +
                ", aadharPhotoFrontUrl='" + aadharPhotoFrontUrl + '\'' +
                ", aadharPhotoBackUrl='" + aadharPhotoBackUrl + '\'' +
                ", panCardPhotoUrl='" + panCardPhotoUrl + '\'' +
                '}';
    }
}
