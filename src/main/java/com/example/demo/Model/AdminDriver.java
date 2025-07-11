package com.example.demo.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class AdminDriver {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

      private String DriverName; 
	
	private String contactNo;
	
	private String drLicenseNo;

    public AdminDriver(int id, String driverName, String contactNo, String drLicenseNo) {
        this.id = id;
        this.DriverName = driverName;
        this.contactNo = contactNo;
        this.drLicenseNo = drLicenseNo;
    }

    public AdminDriver(){
        super();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDriverName() {
        return DriverName;
    }

    public void setDriverName(String driverName) {
        DriverName = driverName;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getDrLicenseNo() {
        return drLicenseNo;
    }

    public void setDrLicenseNo(String drLicenseNo) {
        this.drLicenseNo = drLicenseNo;
    }

   



}
