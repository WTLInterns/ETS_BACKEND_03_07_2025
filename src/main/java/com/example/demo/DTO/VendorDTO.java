package com.example.demo.DTO;

public class VendorDTO {
    
    private Long id;
    private String vendorCompanyName;
    private String contactNo;
    private String alternateMobileNo;
    private String city;
    private String vendorEmail;

    public VendorDTO(Long id, String vendorCompanyName, String contactNo, String alternateMobileNo, String city,
            String vendorEmail) {
        this.id = id;
        this.vendorCompanyName = vendorCompanyName;
        this.contactNo = contactNo;
        this.alternateMobileNo = alternateMobileNo;
        this.city = city;
        this.vendorEmail = vendorEmail;
    }


    public VendorDTO(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVendorCompanyName() {
        return vendorCompanyName;
    }

    public void setVendorCompanyName(String vendorCompanyName) {
        this.vendorCompanyName = vendorCompanyName;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getAlternateMobileNo() {
        return alternateMobileNo;
    }

    public void setAlternateMobileNo(String alternateMobileNo) {
        this.alternateMobileNo = alternateMobileNo;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getVendorEmail() {
        return vendorEmail;
    }

    public void setVendorEmail(String vendorEmail) {
        this.vendorEmail = vendorEmail;
    }

    

    
}
