package com.example.demo.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;
import org.json.JSONObject;


@Service
public class GeocodingService {

    private final String API_KEY = "AIzaSyCelDo4I5cPQ72TfCTQW-arhPZ7ALNcp8w";

    public Map<String, Double> getLatLngFromAddress(String address) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" 
                + UriUtils.encode(address, StandardCharsets.UTF_8)
                + "&key=" + API_KEY;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JSONObject json = new JSONObject(response.getBody());
                JSONObject location = json.getJSONArray("results")
                                          .getJSONObject(0)
                                          .getJSONObject("geometry")
                                          .getJSONObject("location");

                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");

                Map<String, Double> coordinates = new HashMap<>();
                coordinates.put("latitude", lat);
                coordinates.put("longitude", lng);
                return coordinates;
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse geocoding response");
            }
        } else {
            throw new RuntimeException("Geocoding API call failed");
        }
    }
}
