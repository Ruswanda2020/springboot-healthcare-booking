package com.oneDev.healthcarebooking.repository;

import com.oneDev.healthcarebooking.entity.Payment;
import com.oneDev.healthcarebooking.enumaration.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query(value = "SELECT * FROM payment  WHERE appointment_id = :appointmentId FOR UPDATE", nativeQuery = true)
    Optional<Payment> findByAppointmentIdAndLock(@Param("appointmentId") Long appointmentId);

    @Query(value = "SELECT * FROM payment  WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Payment> findByIdLock(@Param("id") Long id);

    @Query("SELECT p FROM Payment p  WHERE p.appointmentId = :appointmentId AND p.status = 'COMPLETED'")
    Optional<Payment> findCompletedPaymentByAppointmentId(@Param("appointmentId") Long appointmentId);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findByAppointmentId(Long appointmentId);
}
