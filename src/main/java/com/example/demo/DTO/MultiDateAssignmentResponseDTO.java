package com.example.demo.DTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiDateAssignmentResponseDTO {
    private boolean overallSuccess;
    private String overallMessage;
    private Map<LocalDate, DriverAssignmentResponseDTO> dateResults;
    private List<LocalDate> successfulDates;
    private List<LocalDate> failedDates;

    public MultiDateAssignmentResponseDTO() {
        this.dateResults = new HashMap<>();
        this.successfulDates = new ArrayList<>();
        this.failedDates = new ArrayList<>();
    }

    public void addDateResult(LocalDate date, DriverAssignmentResponseDTO response) {
        dateResults.put(date, response);
        if (response.isSuccess()) {
            successfulDates.add(date);
        } else {
            failedDates.add(date);
        }
        updateOverallStatus();
    }

    private void updateOverallStatus() {
        if (failedDates.isEmpty()) {
            overallSuccess = true;
            overallMessage = "Driver successfully assigned for all " + successfulDates.size() + " dates";
        } else if (successfulDates.isEmpty()) {
            overallSuccess = false;
            overallMessage = "Driver assignment failed for all " + failedDates.size() + " dates";
        } else {
            overallSuccess = false;
            overallMessage = "Partial success: " + successfulDates.size() + " dates assigned, " +
                    failedDates.size() + " dates failed";
        }
    }

    public boolean isOverallSuccess() { return overallSuccess; }
    public void setOverallSuccess(boolean overallSuccess) { this.overallSuccess = overallSuccess; }

    public String getOverallMessage() { return overallMessage; }
    public void setOverallMessage(String overallMessage) { this.overallMessage = overallMessage; }

    public Map<LocalDate, DriverAssignmentResponseDTO> getDateResults() { return dateResults; }
    public void setDateResults(Map<LocalDate, DriverAssignmentResponseDTO> dateResults) { this.dateResults = dateResults; }

    public List<LocalDate> getSuccessfulDates() { return successfulDates; }
    public void setSuccessfulDates(List<LocalDate> successfulDates) { this.successfulDates = successfulDates; }

    public List<LocalDate> getFailedDates() { return failedDates; }
    public void setFailedDates(List<LocalDate> failedDates) { this.failedDates = failedDates; }
}