package com.example.demo.Model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity
public class ScheduledDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private String status = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonBackReference
    private SchedulingBooking schedulingBooking;

    private String slotId;

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }



    public ScheduledDate(){
        super();
    }


    public ScheduledDate(Long id, LocalDate date, String status, SchedulingBooking schedulingBooking) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.schedulingBooking = schedulingBooking;
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

    public SchedulingBooking getSchedulingBooking() {
        return schedulingBooking;
    }

    public void setSchedulingBooking(SchedulingBooking schedulingBooking) {
        this.schedulingBooking = schedulingBooking;
    }


}