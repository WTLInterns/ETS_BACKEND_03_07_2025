package com.example.demo.Repository;

import com.example.demo.Model.SchedulingBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleBookingRepository extends JpaRepository<SchedulingBooking, Integer> {

    List<SchedulingBooking> findByVendorDriverId(int vendorDriverId);
    List<SchedulingBooking> findBySlotId(String slotId);
    List<SchedulingBooking> findByCarRentalUserId(int carRentalUserId);
        List<SchedulingBooking> findByVendorId(int vendorId);


    long countBySlotIdAndCabType(String slotId, String cabType);

    List<SchedulingBooking> findByVendorDriverIdAndSlotId(int vendorDriverId, String slotId);


    @Query("SELECT sb FROM SchedulingBooking sb JOIN sb.scheduledDates sd WHERE sb.vendorDriverId = :vendorDriverId AND sd.date = :date")
    List<SchedulingBooking> findByVendorDriverIdAndScheduledDatesDate
            (@Param("vendorDriverId") int vendorDriverId, @Param("date") LocalDate date);

}