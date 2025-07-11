package com.example.demo.DTO;

public class AdminCabDTO {
    

    private Long id;


   private String vehicleNameAndRegNo;
    private String vehicleRcNo;
    private String carOtherDetails;

    public AdminCabDTO(){
        super();
    }

    public AdminCabDTO(Long id, String vehicleNameAndRegNo, String vehicleRcNo, String carOtherDetails) {
        this.id = id;
        this.vehicleNameAndRegNo = vehicleNameAndRegNo;
        this.vehicleRcNo = vehicleRcNo;
        this.carOtherDetails = carOtherDetails;
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
