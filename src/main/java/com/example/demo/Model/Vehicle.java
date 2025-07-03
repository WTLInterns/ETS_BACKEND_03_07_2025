package com.example.demo.Model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String model;
    private String make;
    private String vehicleNumber;
    private String cabType;
    private Integer seatingCapacity;
    private Double ratePerKm;
    private String documentUrl;
    private Boolean active;
    
    // Additional fields required by VehicleController
    private String vehicleNo;
    private String vehicleCategory;
    private String brand;
    private String modelType;
    private String fuelType;
    private String vehicleOwnership;
    private Date registrationDate;
    private Date insuranceValidUpto;
    
    // Image URLs
    private String insuranceImageCopy;
    private String registrationCertificateFront;
    private String registrationCertificateBack;
    private String carNumberPhoto;
    
    // Default constructor
    public Vehicle() {
    }
    
    // Parameterized constructor
    public Vehicle(String model, String make, String vehicleNumber, String cabType, 
                  Integer seatingCapacity, Double ratePerKm, String documentUrl, Boolean active) {
        this.model = model;
        this.make = make;
        this.vehicleNumber = vehicleNumber;
        this.cabType = cabType;
        this.seatingCapacity = seatingCapacity;
        this.ratePerKm = ratePerKm;
        this.documentUrl = documentUrl;
        this.active = active;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getMake() {
        return make;
    }
    
    public void setMake(String make) {
        this.make = make;
    }
    
    public String getVehicleNumber() {
        return vehicleNumber;
    }
    
    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
    
    public String getCabType() {
        return cabType;
    }
    
    public void setCabType(String cabType) {
        this.cabType = cabType;
    }
    
    public Integer getSeatingCapacity() {
        return seatingCapacity;
    }
    
    public void setSeatingCapacity(Integer seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }
    
    public Double getRatePerKm() {
        return ratePerKm;
    }
    
    public void setRatePerKm(Double ratePerKm) {
        this.ratePerKm = ratePerKm;
    }
    
    public String getDocumentUrl() {
        return documentUrl;
    }
    
    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    // Additional getters and setters
    public String getVehicleNo() {
        return vehicleNo != null ? vehicleNo : vehicleNumber;
    }
    
    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
        if (this.vehicleNumber == null) {
            this.vehicleNumber = vehicleNo;
        }
    }
    
    public String getVehicleCategory() {
        return vehicleCategory != null ? vehicleCategory : cabType;
    }
    
    public void setVehicleCategory(String vehicleCategory) {
        this.vehicleCategory = vehicleCategory;
        if (this.cabType == null) {
            this.cabType = vehicleCategory;
        }
    }
    
    public String getBrand() {
        return brand != null ? brand : make;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
        if (this.make == null) {
            this.make = brand;
        }
    }
    
    public String getModelType() {
        return modelType != null ? modelType : model;
    }
    
    public void setModelType(String modelType) {
        this.modelType = modelType;
        if (this.model == null) {
            this.model = modelType;
        }
    }
    
    public String getFuelType() {
        return fuelType;
    }
    
    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }
    
    public String getVehicleOwnership() {
        return vehicleOwnership;
    }
    
    public void setVehicleOwnership(String vehicleOwnership) {
        this.vehicleOwnership = vehicleOwnership;
    }
    
    public Date getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    public Date getInsuranceValidUpto() {
        return insuranceValidUpto;
    }
    
    public void setInsuranceValidUpto(Date insuranceValidUpto) {
        this.insuranceValidUpto = insuranceValidUpto;
    }
    
    public String getInsuranceImageCopy() {
        return insuranceImageCopy;
    }
    
    public void setInsuranceImageCopy(String insuranceImageCopy) {
        this.insuranceImageCopy = insuranceImageCopy;
    }
    
    public String getRegistrationCertificateFront() {
        return registrationCertificateFront;
    }
    
    public void setRegistrationCertificateFront(String registrationCertificateFront) {
        this.registrationCertificateFront = registrationCertificateFront;
    }
    
    public String getRegistrationCertificateBack() {
        return registrationCertificateBack;
    }
    
    public void setRegistrationCertificateBack(String registrationCertificateBack) {
        this.registrationCertificateBack = registrationCertificateBack;
    }
    
    public String getCarNumberPhoto() {
        return carNumberPhoto;
    }
    
    public void setCarNumberPhoto(String carNumberPhoto) {
        this.carNumberPhoto = carNumberPhoto;
    }
    
    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", model='" + model + '\'' +
                ", make='" + make + '\'' +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                ", cabType='" + cabType + '\'' +
                ", seatingCapacity=" + seatingCapacity +
                ", ratePerKm=" + ratePerKm +
                ", documentUrl='" + documentUrl + '\'' +
                ", active=" + active +
                ", vehicleNo='" + vehicleNo + '\'' +
                ", vehicleCategory='" + vehicleCategory + '\'' +
                ", brand='" + brand + '\'' +
                ", modelType='" + modelType + '\'' +
                ", fuelType='" + fuelType + '\'' +
                ", vehicleOwnership='" + vehicleOwnership + '\'' +
                ", registrationDate=" + registrationDate +
                ", insuranceValidUpto=" + insuranceValidUpto +
                ", insuranceImageCopy='" + insuranceImageCopy + '\'' +
                ", registrationCertificateFront='" + registrationCertificateFront + '\'' +
                ", registrationCertificateBack='" + registrationCertificateBack + '\'' +
                ", carNumberPhoto='" + carNumberPhoto + '\'' +
                '}';
    }
}
