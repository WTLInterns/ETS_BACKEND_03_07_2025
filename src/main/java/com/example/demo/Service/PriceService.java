package com.example.demo.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.cloudinary.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Model.Price;
import com.example.demo.Repository.PriceRepository;

@Service
public class PriceService {
    
    private final String apiKey = "AIzaSyCelDo4I5cPQ72TfCTQW-arhPZ7ALNcp8w"; // Replace with your Google API key


    @Autowired
    private PriceRepository priceRepository;

    public Price addPrice(Price price){
        return this.priceRepository.save(
            price
        );
    }




   public Price findPriceByPickupAndDrop(String pickupLocation, String dropLocation) {
    if (pickupLocation == null || dropLocation == null) {
        throw new RuntimeException("Pickup and Drop locations must not be null.");
    }

    String[] pickupParts = pickupLocation.split(",");
    if (pickupParts.length < 2) {
        throw new RuntimeException("Invalid pickup location format. Expected format: City,State,Country");
    }
    String sourceCity = pickupParts[0].trim();
    String sourceState = pickupParts[1].trim();

    String[] dropParts = dropLocation.split(",");
    if (dropParts.length < 2) {
        throw new RuntimeException("Invalid drop location format. Expected format: City,State,Country");
    }
    String destinationCity = dropParts[0].trim();
    String destinationState = dropParts[1].trim();

    // Get the distance between pickup and drop locations (in km)
    double distanceInKm = getDistanceInKm(pickupLocation, dropLocation);

    // Optional: Print or log the distance
    System.out.println("Distance between Pickup and Drop Location: " + distanceInKm + " km");

    Price price = priceRepository.findBySourceCityAndSourceStateAndDestinationCityAndDesitnationState(
            sourceCity, sourceState, destinationCity, destinationState
    );

    if (price != null) {
        price.setDistance(distanceInKm);
        price.setPickupLocation(pickupLocation);
        price.setDropLocation(dropLocation);
        return price;
    } else {
        Price defaultPrice = new Price();
        defaultPrice.setSourceCity(sourceCity);
        defaultPrice.setSourceState(sourceState);
        defaultPrice.setDestinationCity(destinationCity);
        defaultPrice.setDesitnationState(destinationState);

        defaultPrice.setHatchback(15);
        defaultPrice.setSedan(17);
        defaultPrice.setSuv(22);
        defaultPrice.setDistance(distanceInKm);
        defaultPrice.setPickupLocation(pickupLocation);
        defaultPrice.setDropLocation(dropLocation);

        // You can use the distance here if you want to adjust pricing based on distance
        // For example, adjust the base price based on distance:
        // double adjustedHatchbackPrice = 15 + (distanceInKm * 0.5); // 0.5 per km for hatchback

        return defaultPrice;
    }
}

private double getDistanceInKm(String pickupLocation, String dropLocation) {
    try {
        // Format the locations for the API request
        String origin = pickupLocation.replace(" ", "+");
        String destination = dropLocation.replace(" ", "+");

        // Construct the API URL
        String urlString = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" 
                            + origin + "&destinations=" + destination + "&key=" + apiKey;

        // Make the API request
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        // Read the response
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parse the response to get the distance in meters
        JSONObject jsonResponse = new JSONObject(response.toString());
        double distanceInMeters = jsonResponse.getJSONArray("rows")
                                            .getJSONObject(0)
                                            .getJSONArray("elements")
                                            .getJSONObject(0)
                                            .getJSONObject("distance")
                                            .getDouble("value");

        // Convert distance to kilometers
        return distanceInMeters / 1000.0;  // Convert from meters to kilometers
    } catch (Exception e) {
        throw new RuntimeException("Error occurred while calculating distance: " + e.getMessage());
    }
}
    

}
