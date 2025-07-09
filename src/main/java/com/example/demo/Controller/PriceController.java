package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Model.Price;
import com.example.demo.Service.PriceService;

import jakarta.persistence.EntityNotFoundException;

@RestController
public class PriceController {
    
@Autowired
private PriceService priceService;

    @PostMapping("/find")
    public Price getPrice(@RequestParam String pickupLocation, @RequestParam String dropLocation) {
        return priceService.findPriceByPickupAndDrop(pickupLocation, dropLocation);
    }

    @PutMapping("/updatePrice/{id}")
    public ResponseEntity<Price> updatePrice(
            @PathVariable int id,
            @RequestParam String sourceState,
            @RequestParam String destinationState,
            @RequestParam String sourceCity,
            @RequestParam String destinationCity,
            @RequestParam int hatchbackPrice,
            @RequestParam int sedanPrice,
            @RequestParam int sedanPremiumPrice,
            @RequestParam int suvPrice,
            @RequestParam int suvPlusPrice,
            @RequestParam String status) {

        try {
            Price updatedPrice = priceService.updateEtsTripprice(
                id,
                sourceState,
                destinationState,
                sourceCity,
                destinationCity,
                hatchbackPrice,
                sedanPrice,
                sedanPremiumPrice,
                suvPrice,
                suvPlusPrice,
                status
            );
            return ResponseEntity.ok(updatedPrice);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
