package com.example.demo.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "vendors")
public class Vendor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String companyName;
    private String email;
    private String phone;
    private String address;
    private String gstNumber;
    private String documentUrl;
    private Boolean active;
    
    // Additional fields required by the application
    private String city;
    private String alternateMobileNo;
    private String contactNo;
    
    // Default constructor
    public Vendor() {
    }
    
    // Parameterized constructor
    public Vendor(String name, String companyName, String email, String phone, 
                 String address, String gstNumber, String documentUrl, Boolean active) {
        this.name = name;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.gstNumber = gstNumber;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getGstNumber() {
        return gstNumber;
    }
    
    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
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
    
    // Additional getters and setters required by the application
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getAlternateMobileNo() {
        return alternateMobileNo;
    }
    
    public void setAlternateMobileNo(String alternateMobileNo) {
        this.alternateMobileNo = alternateMobileNo;
    }
    
    public String getContactNo() {
        return contactNo != null ? contactNo : phone;
    }
    
    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }
    
    // Alias methods for compatibility
    public String getVendorCompanyName() {
        return companyName;
    }
    
    public String getVendorEmail() {
        return email;
    }
    
    @Override
    public String toString() {
        return "Vendor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", companyName='" + companyName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", gstNumber='" + gstNumber + '\'' +
                ", documentUrl='" + documentUrl + '\'' +
                ", active=" + active +
                ", city='" + city + '\'' +
                ", alternateMobileNo='" + alternateMobileNo + '\'' +
                ", contactNo='" + contactNo + '\'' +
                '}';
    }
}
