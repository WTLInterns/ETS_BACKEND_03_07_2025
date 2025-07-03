package com.example.demo.Controller;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Model.Driver;
import com.example.demo.Service.CloudinaryService;
import com.example.demo.Service.DriverService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/driver")
public class DriverController {

    private final DriverService driverService;
    private final CloudinaryService cloudinaryService;

    public DriverController(DriverService driverService,
                            CloudinaryService cloudinaryService) {
        this.driverService     = driverService;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping(
        path     = "/add",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Driver> addDriver(
        @RequestParam(value = "driverImg",          required = false) MultipartFile driverImg,
        @RequestParam(value = "licenseFrontImg",    required = false) MultipartFile licenseFrontImg,
        @RequestParam(value = "licenseBackImg",     required = false) MultipartFile licenseBackImg,
        @RequestParam(value = "idProofFrontImg",    required = false) MultipartFile idProofFrontImg,
        @RequestParam(value = "idProofBackImg",     required = false) MultipartFile idProofBackImg,
        @RequestParam(value = "pccFormImg",         required = false) MultipartFile pccFormImg,
        @RequestParam("driverName")                 String driverName,
        @RequestParam("mobileNumber")               Long mobileNumber,
        @RequestParam("dob")                        String dob,
        @RequestParam("licenseIdNum")               String licenseIdNum,
        @RequestParam("licenseExpiryDate")          Long licenseExpiryDate,
        @RequestParam("idProofType")                String idProofType
    ) throws IOException {
        String driverImgUrl       = (driverImg       != null && !driverImg.isEmpty())
            ? cloudinaryService.upload(driverImg)       : null;
        String licenseFrontUrl    = (licenseFrontImg != null && !licenseFrontImg.isEmpty())
            ? cloudinaryService.upload(licenseFrontImg) : null;
        String licenseBackUrl     = (licenseBackImg  != null && !licenseBackImg.isEmpty())
            ? cloudinaryService.upload(licenseBackImg)  : null;
        String idProofFrontUrl    = (idProofFrontImg != null && !idProofFrontImg.isEmpty())
            ? cloudinaryService.upload(idProofFrontImg) : null;
        String idProofBackUrl     = (idProofBackImg  != null && !idProofBackImg.isEmpty())
            ? cloudinaryService.upload(idProofBackImg)  : null;
        String pccFormUrl         = (pccFormImg      != null && !pccFormImg.isEmpty())
            ? cloudinaryService.upload(pccFormImg)      : null;
            String status = "DRIVER";

        Driver driver = new Driver(
            driverName,
            mobileNumber,
            driverImgUrl,
            dob,
            licenseIdNum,
            licenseExpiryDate,
            licenseFrontUrl,
            licenseBackUrl,
            idProofType,
            idProofFrontUrl,
            idProofBackUrl,
            pccFormUrl,
            status

        );

        Driver saved = driverService.saveDriver(driver);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/allDriver")
    public List<Driver> getAllDriver(){
        return this.driverService.getAllDriver();
    }

    @GetMapping("/driver/{id}")
    public Driver getDriverById(@PathVariable int id){
        return this.driverService.getDriverById(id);
    }
}
