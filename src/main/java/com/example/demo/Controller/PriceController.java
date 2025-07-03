package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Model.Price;
import com.example.demo.Service.PriceService;

@RestController
public class PriceController {
    
@Autowired
private PriceService priceService;

    @PostMapping("/find")
    public Price getPrice(@RequestParam String pickupLocation, @RequestParam String dropLocation) {
        return priceService.findPriceByPickupAndDrop(pickupLocation, dropLocation);
    }
}
