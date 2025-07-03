package com.example.demo.DTO;

public class UserLoginRequestDTO {
    private String mobileNo;
    private String password;

    public UserLoginRequestDTO(){
        super();
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    
}
