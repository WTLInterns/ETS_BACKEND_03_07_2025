package com.example.demo.DTO;

import java.time.LocalDate;
import java.util.List;

public class SlotDTO {
    private String slotId;
    private LocalDate date;
    private int totalBookings;
    private List<SlotBookingDTO> bookings;

    public SlotDTO() {}

    public SlotDTO(String slotId, LocalDate date, int totalBookings, List<SlotBookingDTO> bookings) {
        this.slotId = slotId;
        this.date = date;
        this.totalBookings = totalBookings;
        this.bookings = bookings;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public List<SlotBookingDTO> getBookings() {
        return bookings;
    }

    public void setBookings(List<SlotBookingDTO> bookings) {
        this.bookings = bookings;
    }
}
