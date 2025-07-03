package com.example.demo.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "car_rental_users")
public class CarRentalUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String userName;
    private String email;
    private String phone;
    private String address;
    private Boolean active;
    
    // Additional fields based on error messages
    private String lastName;
    private String gender;
    
    // Default constructor
    public CarRentalUser() {
    }
    
    // Parameterized constructor
    public CarRentalUser(String name, String email, String phone, String address, Boolean active) {
        this.userName = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.active = active;
    }
    
    // Extended parameterized constructor
    public CarRentalUser(String name, String lastName, String email, String phone, 
                        String address, String gender, Boolean active) {
        this.userName = name;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.gender = gender;
        this.active = active;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return userName;
    }
    
    public void setName(String name) {
        this.userName = name;
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
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    // Alias methods for compatibility
    public String getUserName() {
        return userName;
    }
    
    @Override
    public String toString() {
        return "CarRentalUser{" +
                "id=" + id +
                ", name='" + userName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", gender='" + gender + '\'' +
                ", active=" + active +
                '}';
    }
}
