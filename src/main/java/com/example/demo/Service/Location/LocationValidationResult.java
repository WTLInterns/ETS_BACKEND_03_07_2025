package com.example.demo.Service.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationValidationResult {
    private boolean valid;
    private String message;
    private String errorMessage;
    private String errorCode;
    private List<String> suggestions;
    private String validationMethod;
    private Double latitude;
    private Double longitude;

    public LocationValidationResult() {
        this.suggestions = new ArrayList<>();
    }

    // Factory methods for success
    public static LocationValidationResult success(String message) {
        LocationValidationResult result = new LocationValidationResult();
        result.setValid(true);
        result.setMessage(message);
        return result;
    }

    public static LocationValidationResult success(String message, String validationMethod) {
        LocationValidationResult result = success(message);
        result.setValidationMethod(validationMethod);
        return result;
    }

    public static LocationValidationResult success(String message, String validationMethod, double lat, double lng) {
        LocationValidationResult result = success(message, validationMethod);
        result.setLatitude(lat);
        result.setLongitude(lng);
        return result;
    }

    // Factory methods for errors
    public static LocationValidationResult error(String errorCode, String errorMessage) {
        LocationValidationResult result = new LocationValidationResult();
        result.setValid(false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        return result;
    }

    public static LocationValidationResult error(String errorCode, String errorMessage, List<String> suggestions) {
        LocationValidationResult result = error(errorCode, errorMessage);
        result.setSuggestions(suggestions);
        return result;
    }

    // Getters and setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
    }

    public String getValidationMethod() {
        return validationMethod;
    }

    public void setValidationMethod(String validationMethod) {
        this.validationMethod = validationMethod;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "LocationValidationResult{" +
                "valid=" + valid +
                ", message='" + message + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", validationMethod='" + validationMethod + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", suggestions=" + suggestions +
                '}';
    }
}