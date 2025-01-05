package com.oneDev.healthcarebooking.service;

import com.oneDev.healthcarebooking.entity.Appointment;
import com.oneDev.healthcarebooking.model.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse createPayment (Appointment appointment);
    PaymentResponse findByAppointmentId(Long appointmentId);
    PaymentResponse cancelPayment (Long paymentId);
    PaymentResponse recalculatePayment (Appointment updateAppointment);
    PaymentResponse cancelPaymentForAppointment (Long appointmentId);
}
