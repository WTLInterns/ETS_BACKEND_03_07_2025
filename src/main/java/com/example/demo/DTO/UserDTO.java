package com.example.demo.DTO;

public class UserDTO {
    

    private int id;

    private String userName;

    private String lastName;

    private String email;

    private String gender;

    private String phone;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserDTO(int id, String userName, String lastName, String email, String gender, String phone) {
        this.id = id;
        this.userName = userName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
        this.phone = phone;
    }    


    public UserDTO(){
        super();
    }
    

    
}
