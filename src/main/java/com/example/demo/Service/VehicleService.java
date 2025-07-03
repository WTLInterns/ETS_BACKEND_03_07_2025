package com.example.demo.Service;

import com.example.demo.Model.Vehicle;
import com.example.demo.Repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private CloudinaryService cloudinaryService;

    public Vehicle saveVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }

    public Vehicle handleVehicleImagesAndSave(Vehicle vehicle,
                                              MultipartFile insuranceImageCopy,
                                              MultipartFile registrationCertificateFront,
                                              MultipartFile registrationCertificateBack,
                                              MultipartFile carNumberPhoto) throws IOException {
        if (insuranceImageCopy != null && !insuranceImageCopy.isEmpty()) {
            vehicle.setInsuranceImageCopy(cloudinaryService.upload(insuranceImageCopy));
        }
        if (registrationCertificateFront != null && !registrationCertificateFront.isEmpty()) {
            vehicle.setRegistrationCertificateFront(cloudinaryService.upload(registrationCertificateFront));
        }
        if (registrationCertificateBack != null && !registrationCertificateBack.isEmpty()) {
            vehicle.setRegistrationCertificateBack(cloudinaryService.upload(registrationCertificateBack));
        }
        if (carNumberPhoto != null && !carNumberPhoto.isEmpty()) {
            vehicle.setCarNumberPhoto(cloudinaryService.upload(carNumberPhoto));
        }
        return saveVehicle(vehicle);
    }
}
