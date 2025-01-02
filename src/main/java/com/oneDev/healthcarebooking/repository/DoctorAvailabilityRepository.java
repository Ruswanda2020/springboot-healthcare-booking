package com.oneDev.healthcarebooking.repository;

import com.oneDev.healthcarebooking.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    @Query(value = "SELECT * FROM doctor_availability" +
            " WHERE doctor_id = :doctorId AND date >= CURRENT_DATE " +
            "ORDER BY date ASC, start_time ASC",
            nativeQuery = true)
    List<DoctorAvailability> findAvailabilityByDoctorIdFromToday(@Param("doctorId") Long doctorId);
}
