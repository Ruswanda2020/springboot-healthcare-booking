package com.oneDev.healthcarebooking.repository;

import com.oneDev.healthcarebooking.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query(value = "SELECT * FROM appointment WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Appointment> findByIdLock(@Param("id") Long id);

    @Query(value = "SELECT * FROM appointment " +
            "WHERE doctor_id = :doctorId " +
            "AND appointment_date = :date " +
            "AND consultation_type = :consultationType " +
            "AND status = 'SCHEDULED' " +
            "AND ((start_time < :endTime AND end_time > :startTime) " +
            "OR (start_time = :startTime AND end_time = :endTime)) " +
            "FOR UPDATE",
            nativeQuery = true)
    List<Appointment> findOverlappingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("consultationType") String consultationType
    );


    @Query(value = "SELECT * FROM appointment " +
            "WHERE patient_id = :patientId " +
            "ORDER BY appointment_date DESC, start_time DESC ",
            nativeQuery = true)
    List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(
            @Param("patientId")Long patientId);

    @Query(value = "SELECT * FROM appointment " +
            "WHERE doctor_id = :doctorId " +
            "AND appointment_date = :appointmentDate " +
            "ORDER BY start_time ASC ",
            nativeQuery = true)
    List<Appointment> findByDoctorIdAndAppointmentDateOrderByStartTimeAsc(
            @Param("doctorId")Long doctorId,
            @Param("appointmentDate") LocalDate appointmentDate);


    @Query(value = "SELECT * FROM appointment " +
            "WHERE doctor_id = :doctorId " +
            "AND appointment_date >= :startDate " +
            "AND appointment_date <= :endDate " +
            "ORDER BY oppointment_date ASC, start_date ASC ",
            nativeQuery = true)
    List<Appointment> findDoctorAppointmentInDateRange(
      @Param("doctorId") Long doctorId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
    );
}
