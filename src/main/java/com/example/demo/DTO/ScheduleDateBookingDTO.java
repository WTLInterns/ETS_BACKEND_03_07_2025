package com.example.demo.DTO;

import java.time.LocalDate;

public class ScheduleDateBookingDTO {


    private Long id;

    private LocalDate date;

    private String status = "PENDING";

    private String slotId;

    public ScheduleDateBookingDTO(){
        super();

    }

    public ScheduleDateBookingDTO(Long id, LocalDate date, String status, String slotId) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.slotId=slotId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }
}
