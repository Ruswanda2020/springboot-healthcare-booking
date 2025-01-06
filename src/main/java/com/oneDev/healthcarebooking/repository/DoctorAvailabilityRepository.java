package com.oneDev.healthcarebooking.repository;

import com.oneDev.healthcarebooking.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    @Query(value = "SELECT * FROM doctor_availability " +
            "WHERE doctor_id = :doctorId AND date >= CURRENT_DATE " +
            "ORDER BY date ASC, start_time ASC",
            nativeQuery = true)
    List<DoctorAvailability> findAvailabilityByDoctorIdFromToday(@Param("doctorId") Long doctorId);

    @Query(value = "SELECT * FROM doctor_availability " +
            "WHERE doctor_id = :doctorId " +
            "AND date BETWEEN :startDate AND :endDate " +
            "ORDER BY date ASC, start_time ASC",
            nativeQuery = true)
    List<DoctorAvailability> findAvailableSlotByDoctorAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "SELECT COUNT(*) > 0 " +
            "FROM doctor_availability " +
            "WHERE doctor_id = :doctorId " +
            "AND date = :date " +
            "AND start_time <= :startTime " +
            "AND end_time >= :endTime " +
            "AND LOWER(consultation_type) = LOWER(:consultationType) " +
            "AND is_available = true",
            nativeQuery = true)
    boolean isDoctorAvailable(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("consultationType") String consultationType
    );

    @Query(value = "SELECT COUNT(*) > 0 " +
            "FROM doctor_availability " +
            "WHERE doctor_id = :doctorId " +
            "AND date = :date " +
            "AND start_time = :startTime " +
            "AND end_time = :endTime " +
            "AND consultation_type = :consultationType " +
            "AND is_available = true",
            nativeQuery = true)
    boolean existsDuplicateAvailability(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("consultationType") String consultationType
    );
}
