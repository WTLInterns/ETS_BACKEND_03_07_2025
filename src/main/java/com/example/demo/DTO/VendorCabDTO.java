package com.example.demo.DTO;

public class VendorCabDTO {
    

    private int vendorCabId;

	private String carName;



	private String vehicleNo;



    public VendorCabDTO(int vendorCabId, String carName, String vehicleNo) {
        this.vendorCabId = vendorCabId;
        this.carName = carName;
        this.vehicleNo = vehicleNo;
    }

    public VendorCabDTO(){
        super();
    }



    public int getVendorCabId() {
        return vendorCabId;
    }



    public void setVendorCabId(int vendorCabId) {
        this.vendorCabId = vendorCabId;
    }



    public String getCarName() {
        return carName;
    }



    public void setCarName(String carName) {
        this.carName = carName;
    }



    public String getVehicleNo() {
        return vehicleNo;
    }



    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }


    



}
