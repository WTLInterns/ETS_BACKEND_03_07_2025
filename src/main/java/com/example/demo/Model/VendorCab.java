package com.example.demo.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "vendorCab")
public class VendorCab {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int vendorCabId;

	private String carName;

    private String vehicleNo;

public VendorCab(int vendorCabId, String carName, String vehicleNo) {
    this.vendorCabId = vendorCabId;
    this.carName = carName;
    this.vehicleNo = vehicleNo;
}


public VendorCab(){
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
