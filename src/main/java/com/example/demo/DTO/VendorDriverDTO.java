package com.example.demo.DTO;

public class VendorDriverDTO {
    
    private int vendorDriverId;

	private String driverName;

	private String contactNo;

	private String altContactNo;

	private String address;

    public VendorDriverDTO(int vendorDriverId, String driverName, String contactNo, String altContactNo,
            String address) {
        this.vendorDriverId = vendorDriverId;
        this.driverName = driverName;
        this.contactNo = contactNo;
        this.altContactNo = altContactNo;
        this.address = address;
    }

    public VendorDriverDTO(){
        super();
    }

    public int getVendorDriverId() {
        return vendorDriverId;
    }

    public void setVendorDriverId(int vendorDriverId) {
        this.vendorDriverId = vendorDriverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getAltContactNo() {
        return altContactNo;
    }

    public void setAltContactNo(String altContactNo) {
        this.altContactNo = altContactNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    
}
