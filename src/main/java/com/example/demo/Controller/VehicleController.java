package com.example.demo.Controller;

import com.example.demo.Model.Vehicle;
import com.example.demo.Service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {
    @Autowired
    private VehicleService vehicleService;

    
    @PostMapping(
        path     = "/add",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Vehicle> addVehicle(
            @RequestParam String vehicleNo,
            @RequestParam String vehicleCategory,
            @RequestParam String brand,
            @RequestParam String modelType,
            @RequestParam String fuelType,
            @RequestParam String vehicleOwnership,
            @RequestParam String registrationDate,
            @RequestParam String insuranceValidUpto,
            @RequestParam(value = "insuranceImageCopy", required = false) MultipartFile insuranceImageCopy,
            @RequestParam(value = "registrationCertificateFront", required = false) MultipartFile registrationCertificateFront,
            @RequestParam(value = "registrationCertificateBack", required = false) MultipartFile registrationCertificateBack,
            @RequestParam(value = "carNumberPhoto", required = false) MultipartFile carNumberPhoto
    ) throws IOException {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNo(vehicleNo);
        vehicle.setVehicleCategory(vehicleCategory);
        vehicle.setBrand(brand);
        vehicle.setModelType(modelType);
        vehicle.setFuelType(fuelType);
        vehicle.setVehicleOwnership(vehicleOwnership);
        vehicle.setRegistrationDate(java.sql.Date.valueOf(registrationDate));
        vehicle.setInsuranceValidUpto(java.sql.Date.valueOf(insuranceValidUpto));
        Vehicle saved = vehicleService.handleVehicleImagesAndSave(vehicle, insuranceImageCopy, registrationCertificateFront, registrationCertificateBack, carNumberPhoto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleService.getAllVehicles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicle(@PathVariable Long id) {
        Optional<Vehicle> vehicle = vehicleService.getVehicleById(id);
        return vehicle.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(
            @PathVariable Long id,
            @RequestParam String vehicleNo,
            @RequestParam String vehicleCategory,
            @RequestParam String brand,
            @RequestParam String modelType,
            @RequestParam String fuelType,
            @RequestParam String vehicleOwnership,
            @RequestParam String registrationDate,
            @RequestParam String insuranceValidUpto,
            @RequestParam(value = "insuranceImageCopy", required = false) MultipartFile insuranceImageCopy,
            @RequestParam(value = "registrationCertificateFront", required = false) MultipartFile registrationCertificateFront,
            @RequestParam(value = "registrationCertificateBack", required = false) MultipartFile registrationCertificateBack,
            @RequestParam(value = "carNumberPhoto", required = false) MultipartFile carNumberPhoto
    ) throws IOException {
        Optional<Vehicle> optionalVehicle = vehicleService.getVehicleById(id);
        if (!optionalVehicle.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Vehicle vehicle = optionalVehicle.get();
        vehicle.setVehicleNo(vehicleNo);
        vehicle.setVehicleCategory(vehicleCategory);
        vehicle.setBrand(brand);
        vehicle.setModelType(modelType);
        vehicle.setFuelType(fuelType);
        vehicle.setVehicleOwnership(vehicleOwnership);
        vehicle.setRegistrationDate(java.sql.Date.valueOf(registrationDate));
        vehicle.setInsuranceValidUpto(java.sql.Date.valueOf(insuranceValidUpto));
        Vehicle updated = vehicleService.handleVehicleImagesAndSave(vehicle, insuranceImageCopy, registrationCertificateFront, registrationCertificateBack, carNumberPhoto);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
