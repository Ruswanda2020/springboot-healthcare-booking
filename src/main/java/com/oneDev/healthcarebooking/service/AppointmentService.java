package com.oneDev.healthcarebooking.service;

import com.oneDev.healthcarebooking.model.request.AppointmentRequest;
import com.oneDev.healthcarebooking.model.request.AppointmentRescheduleRequest;
import com.oneDev.healthcarebooking.model.response.AppointmentResponse;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    AppointmentResponse bookAppointment(AppointmentRequest request);
    AppointmentResponse rescheduleAppointment(Long userId, Long appointmentId, AppointmentRescheduleRequest request);
    List<AppointmentResponse> listUserAppointments(Long userId);
    void cancelAppointment(Long patientId, Long appointmentId);
    AppointmentResponse findById(Long appointmentId);
    List<AppointmentResponse> listDoctorAppointments(Long doctorId, LocalDate appointmentDate);
}
