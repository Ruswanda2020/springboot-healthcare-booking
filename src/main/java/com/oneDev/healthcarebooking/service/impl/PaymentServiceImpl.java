package com.oneDev.healthcarebooking.service.impl;

import com.oneDev.healthcarebooking.entity.Appointment;
import com.oneDev.healthcarebooking.entity.DoctorSpecialization;
import com.oneDev.healthcarebooking.entity.Payment;
import com.oneDev.healthcarebooking.enumaration.AppointmentStatus;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.enumaration.PaymentStatus;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.response.PaymentResponse;
import com.oneDev.healthcarebooking.repository.AppointmentRepository;
import com.oneDev.healthcarebooking.repository.DoctorSpecializationRepository;
import com.oneDev.healthcarebooking.repository.PaymentRepository;
import com.oneDev.healthcarebooking.service.PaymentService;
import com.oneDev.healthcarebooking.service.XenditService;
import com.oneDev.healthcarebooking.utils.TransactionIdGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorSpecializationRepository specializationRepository;
    private final XenditService xenditService;

    @Override @Transactional
    public PaymentResponse createPayment(Appointment appointment) {

        if(appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Payment can only created for appointment with status PENDING");
        }

        DoctorSpecialization doctorSpecialization = specializationRepository.findById(appointment.getDoctorSpecializationId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Doctor specialize not found with id: " + appointment.getDoctorSpecializationId()));

        BigDecimal hourlyFee = doctorSpecialization.getBaseFee();
        BigDecimal amount = calculatePaymentAmount(appointment, hourlyFee);
        String transactionId = TransactionIdGenerator.generateTransactionId();

        Payment payment = Payment.builder()
                .appointmentId(appointment.getId())
                .paymentMethod("NO_SELECTED")
                .transactionId(transactionId)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();

         paymentRepository.save(payment);
        return xenditService.createPayment(payment);
    }

    @Override
    public PaymentResponse findByAppointmentId(Long appointmentId) {
        Payment savedPayment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Payment with appointment " + appointmentId + " not found"));
        return PaymentResponse.from(savedPayment);
    }

    @Override @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findByIdLock(paymentId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Payment ID with ID : " +  paymentId + "not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only payment PENDING can be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        Payment cancelledPayment = paymentRepository.save(payment);
        return PaymentResponse.from(cancelledPayment);
    }

    @Override @Transactional
    public PaymentResponse recalculatePayment(Appointment updateAppointment) {
        Payment payment = paymentRepository.findByAppointmentIdAndLock(updateAppointment.getId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Payment not found for the appointment"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only payment PENDING can be cancelled");
        }


        DoctorSpecialization doctorSpecialization = specializationRepository.findById(updateAppointment.getDoctorSpecializationId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Doctor specialize not found with id: " + updateAppointment.getDoctorSpecializationId()));

        BigDecimal hourlyFee = doctorSpecialization.getBaseFee();
        BigDecimal newAmount = calculatePaymentAmount(updateAppointment, hourlyFee);
        payment.setAmount(newAmount);

        Payment savedPayment = paymentRepository.save(payment);

        return PaymentResponse.from(savedPayment);
    }

    @Override @Transactional
    public PaymentResponse cancelPaymentForAppointment(Long appointmentId) {
        Payment payment = paymentRepository.findByAppointmentIdAndLock(appointmentId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Appointment ID not found with ID : " + appointmentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only payment PENDING can be cancelled");
        }
        payment.setStatus(PaymentStatus.CANCELLED);
        Payment cancelledPayment = paymentRepository.save(payment);
        return PaymentResponse.from(cancelledPayment);
    }

    private BigDecimal calculatePaymentAmount(Appointment appointment, BigDecimal hourlyFee) {
        Duration duration = Duration.between(appointment.getStartTime(), appointment.getEndTime());
        long hours = duration.toHours();
        if (duration.toMinutesPart() > 0 || duration.toSecondsPart() > 0) {
            hours += 1;
        }
        return hourlyFee.multiply(BigDecimal.valueOf(hours)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
