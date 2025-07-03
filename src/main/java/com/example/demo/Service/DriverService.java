package com.example.demo.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Model.Driver;
import com.example.demo.Repository.DriverReposiotry;

@Service
public class DriverService {
    
    @Autowired
    private DriverReposiotry driverReposiotry;

    @Autowired
    private CloudinaryService cloudinaryService;

    public Driver saveDriver(Driver driver) {
        
        return driverReposiotry.save(driver);
    }


    public List<Driver> getAllDriver(){
        return this.driverReposiotry.findAll();
    }

    public Driver getDriverById(int id){
        return this.driverReposiotry.findById(id).get();
    }

    }
