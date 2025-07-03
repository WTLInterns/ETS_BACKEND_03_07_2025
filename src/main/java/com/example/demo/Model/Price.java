package com.example.demo.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "prices")
public class Price {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String sourceCity;
    private String sourceState;
    private String destinationCity;
    private String desitnationState;
    private Double distance;
    private Double basePrice;
    private Double pricePerKm;
    private String cabType;
    
    // Additional fields needed for the application
    @Transient
    private String pickupLocation;
    @Transient
    private String dropLocation;
    @Transient
    private int hatchback;
    @Transient
    private int sedan;
    @Transient
    private int suv;
    
    // Default constructor
    public Price() {
    }
    
    // Parameterized constructor
    public Price(String sourceCity, String sourceState, String destinationCity, String desitnationState, 
                 Double distance, Double basePrice, Double pricePerKm, String cabType) {
        this.sourceCity = sourceCity;
        this.sourceState = sourceState;
        this.destinationCity = destinationCity;
        this.desitnationState = desitnationState;
        this.distance = distance;
        this.basePrice = basePrice;
        this.pricePerKm = pricePerKm;
        this.cabType = cabType;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getSourceCity() {
        return sourceCity;
    }
    
    public void setSourceCity(String sourceCity) {
        this.sourceCity = sourceCity;
    }
    
    public String getSourceState() {
        return sourceState;
    }
    
    public void setSourceState(String sourceState) {
        this.sourceState = sourceState;
    }
    
    public String getDestinationCity() {
        return destinationCity;
    }
    
    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }
    
    public String getDesitnationState() {
        return desitnationState;
    }
    
    public void setDesitnationState(String desitnationState) {
        this.desitnationState = desitnationState;
    }
    
    public Double getDistance() {
        return distance;
    }
    
    public void setDistance(Double distance) {
        this.distance = distance;
    }
    
    public Double getBasePrice() {
        return basePrice;
    }
    
    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }
    
    public Double getPricePerKm() {
        return pricePerKm;
    }
    
    public void setPricePerKm(Double pricePerKm) {
        this.pricePerKm = pricePerKm;
    }
    
    public String getCabType() {
        return cabType;
    }
    
    public void setCabType(String cabType) {
        this.cabType = cabType;
    }
    
    // Additional getters and setters
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
    
    public int getHatchback() {
        return hatchback;
    }
    
    public void setHatchback(int hatchback) {
        this.hatchback = hatchback;
    }
    
    public int getSedan() {
        return sedan;
    }
    
    public void setSedan(int sedan) {
        this.sedan = sedan;
    }
    
    public int getSuv() {
        return suv;
    }
    
    public void setSuv(int suv) {
        this.suv = suv;
    }
    
    @Override
    public String toString() {
        return "Price{" +
                "id=" + id +
                ", sourceCity='" + sourceCity + '\'' +
                ", sourceState='" + sourceState + '\'' +
                ", destinationCity='" + destinationCity + '\'' +
                ", desitnationState='" + desitnationState + '\'' +
                ", distance=" + distance +
                ", basePrice=" + basePrice +
                ", pricePerKm=" + pricePerKm +
                ", cabType='" + cabType + '\'' +
                ", pickupLocation='" + pickupLocation + '\'' +
                ", dropLocation='" + dropLocation + '\'' +
                ", hatchback=" + hatchback +
                ", sedan=" + sedan +
                ", suv=" + suv +
                '}';
    }
}
