package com.example.demo.DTO;

import java.util.List;

public class DriverSlotsResponseDTO {
    private List<SlotDTO> slots;

    public DriverSlotsResponseDTO() {}

    public DriverSlotsResponseDTO(List<SlotDTO> slots) {
        this.slots = slots;
    }

    public List<SlotDTO> getSlots() {
        return slots;
    }

    public void setSlots(List<SlotDTO> slots) {
        this.slots = slots;
    }
}