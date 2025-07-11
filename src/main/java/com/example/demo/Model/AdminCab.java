package com.example.demo.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AdminCab {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


   private String vehicleNameAndRegNo;
    private String vehicleRcNo;
    private String carOtherDetails;

    public AdminCab(Long id, String vehicleNameAndRegNo, String vehicleRcNo, String carOtherDetails) {
        this.id = id;
        this.vehicleNameAndRegNo = vehicleNameAndRegNo;
        this.vehicleRcNo = vehicleRcNo;
        this.carOtherDetails = carOtherDetails;
    }

    public AdminCab(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleNameAndRegNo() {
        return vehicleNameAndRegNo;
    }

    public void setVehicleNameAndRegNo(String vehicleNameAndRegNo) {
        this.vehicleNameAndRegNo = vehicleNameAndRegNo;
    }

    public String getVehicleRcNo() {
        return vehicleRcNo;
    }

    public void setVehicleRcNo(String vehicleRcNo) {
        this.vehicleRcNo = vehicleRcNo;
    }

    public String getCarOtherDetails() {
        return carOtherDetails;
    }

    public void setCarOtherDetails(String carOtherDetails) {
        this.carOtherDetails = carOtherDetails;
    }

   


    
}
