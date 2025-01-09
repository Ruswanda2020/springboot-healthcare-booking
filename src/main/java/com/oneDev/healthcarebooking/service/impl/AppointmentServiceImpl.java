package com.oneDev.healthcarebooking.service.impl;

import com.oneDev.healthcarebooking.entity.*;
import com.oneDev.healthcarebooking.enumaration.AppointmentStatus;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.request.AppointmentRequest;
import com.oneDev.healthcarebooking.model.request.AppointmentRescheduleRequest;
import com.oneDev.healthcarebooking.model.response.AppointmentResponse;
import com.oneDev.healthcarebooking.model.response.PaymentResponse;
import com.oneDev.healthcarebooking.repository.*;
import com.oneDev.healthcarebooking.service.AppointmentService;
import com.oneDev.healthcarebooking.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    // Repositori yang digunakan untuk berbagai operasi database
    private final AppointmentRepository appointmentRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final HospitalDoctorFeeRepository hospitalDoctorFeeRepository;
    private final DoctorSpecializationRepository doctorSpecializationRepository;
    private final HospitalRepository hospitalRepository;
    private final PaymentService paymentService;

    @Override @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request) {

        // 1. Memastikan User ada di database
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "User not found with id: " + request.getUserId()));

        // 2. Memastikan Doctor ada di database
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Doctor not found with id: " + request.getDoctorId()));

        // 3. Memastikan Hospital terkait doctor ada di database
        Hospital hospital = hospitalRepository.findById(doctor.getHospitalId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Hospital not found with id: " + doctor.getHospitalId()));

        // 4. Memastikan spesialisasi dokter tersedia
        DoctorSpecialization doctorSpecializeId = doctorSpecializationRepository.findById(request.getDoctorSpecializationId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Doctor specialize id not found with id: " + request.getDoctorSpecializationId()));

        // 5. Memastikan biaya konsultasi berdasarkan rumah sakit dan spesialisasi dokter
        HospitalDoctorFee fee = hospitalDoctorFeeRepository
                .findByHospitalIdAndDoctorSpecializationId(doctor.getHospitalId(), doctorSpecializeId.getId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Doctor specialize not found"));

        // 6. Mengecek apakah dokter tersedia pada waktu yang diminta
        boolean isDoctorAvailable = doctorAvailabilityRepository
                .isDoctorAvailable(doctor.getId(),
                        request.getAppointmentDate(),
                        request.getStartTime(),
                        request.getEndTime(),
                        fee.getConsultationType()
                );

        log.info("DoctorAvailability for doctorId: {}, date: {}, startTime: {}, endTime: {}, consultationType: {} - is available: {}",
                doctor.getId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime(),
                fee.getConsultationType(),
                isDoctorAvailable);

        // Jika dokter tidak tersedia, lemparkan exception
        if(!isDoctorAvailable) {
            throw new ApplicationException(ExceptionType.APPOINTMENT_CONFLICT, "Doctor is not available");
        }

        // 7. Memeriksa apakah ada jadwal appointment yang tumpang tindih
        List<Appointment> overlappingAppointments = appointmentRepository.findOverlappingAppointments(
                doctor.getId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime(),
                fee.getConsultationType()
        );

        // Jika ada appointment yang tumpang tindih, lemparkan exception
        if(!overlappingAppointments.isEmpty()) {
            throw new ApplicationException(ExceptionType.APPOINTMENT_CONFLICT,
                    "The selected time slot overlaps with existing appointments of the same consultation type");
        }

        // 8. Membuat entitas appointment baru
        Appointment appointment = Appointment.builder()
                .patientId(request.getUserId())
                .doctorId(request.getDoctorId())
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .hospitalId(doctor.getHospitalId())
                .consultationType(fee.getConsultationType())
                .doctorSpecializationId(request.getDoctorSpecializationId())
                .status(AppointmentStatus.PENDING)
                .build();


        // 9. Menyimpan appointment ke database
        appointmentRepository.save(appointment);

        //10.create payment
        PaymentResponse paymentResponse = paymentService.createPayment(appointment);

        // 11. Membuat response untuk mengembalikan detail appointment ke clien
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(user.getUserId())
                .patientName(user.getUsername())
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .hospitalId(hospital.getId())
                .hospitalName(hospital.getName())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .consultationType(fee.getConsultationType())
                .status(appointment.getStatus())
                .paymentDetails(paymentResponse)
                .build();
    }

    @Override @Transactional
    public AppointmentResponse rescheduleAppointment(
            Long patientId,
            Long appointmentId,
            AppointmentRescheduleRequest request) {

        // 1. Memastikan Appointment Ada di Database
        Appointment appointment = appointmentRepository.findByIdLock(appointmentId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Appointment not found with id: " + appointmentId));

        // 2. Memastikan Appointment Milik Pasien Terkait
        if(!appointment.getPatientId().equals(patientId)){
            throw  new ApplicationException(ExceptionType.FORBIDDEN, "Can't reschedule other appointment");
        }

        // 3. Memastikan Status Appointment
        if(appointment.getStatus() != AppointmentStatus.PENDING &&
                appointment.getStatus() != AppointmentStatus.SCHEDULED){
            throw new IllegalArgumentException("Appointment can't be reschedule");
        }

        // 4. Memeriksa Tanggal Penjadwalan Ulang
        if (request.getAppointmentDate().isBefore(LocalDate.now())){
            throw new IllegalArgumentException("Can't reschedule to past date");
        }

        // 5. Memeriksa Ketersediaan Dokter
        boolean isDoctorAvailable = doctorAvailabilityRepository
                .isDoctorAvailable(appointment.getDoctorId(),
                        request.getAppointmentDate(),
                        request.getStartTime(),
                        request.getEndTime(),
                        appointment.getConsultationType());

        // Jika dokter tidak tersedia
        if(!isDoctorAvailable) {
            throw new ApplicationException(ExceptionType.APPOINTMENT_CONFLICT, "Doctor is not available");
        }

        // 6. Memeriksa Tumpang Tindih dengan Janji Temu Lain
        List<Appointment> overlappingAppointments = appointmentRepository.findOverlappingAppointments(
                appointment.getDoctorId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime(),
                appointment.getConsultationType());

        // Jika ada appointment yang tumpang tindih
        if(!overlappingAppointments.isEmpty()) {
            throw new ApplicationException(ExceptionType.APPOINTMENT_CONFLICT,
                    "The selected time slot overlaps with existing appointments of the same consultation type");
        }

        // 7. Mengubah Waktu dan Tanggal Appointment
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());

        // 8. Menyimpan Perubahan ke Database
        appointmentRepository.save(appointment);

        //9.kalkulasi ulang
        paymentService.recalculatePayment(appointment);

        // 10. Mengembalikan Response
        return convertToResponse(appointment);
    }

    @Override
    public List<AppointmentResponse> listUserAppointments(Long userId) {
        List<Appointment> appointments = appointmentRepository.
                findByPatientIdOrderByAppointmentDateDescStartTimeDesc(userId);
        return appointments.stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override @Transactional
    public void cancelAppointment(Long userId, Long appointmentId) {

        // 1. Memastikan Appointment Ada di Database
        Appointment appointment = appointmentRepository.findByIdLock(appointmentId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Appointment not found with id: " + appointmentId));

        // 2. Memastikan Appointment Milik Pasien Terkait
        if(!appointment.getPatientId().equals(userId)){
            throw  new ApplicationException(ExceptionType.FORBIDDEN, "Can't cancel other appointment");
        }

        // 3. Memastikan Status Appointment
        if(appointment.getStatus() != AppointmentStatus.PENDING){
            throw new IllegalArgumentException("Only status pending can be cancelled");
        }

        // 4. Mengubah Status Appointment Menjadi CANCELLED
        appointment.setStatus(AppointmentStatus.CANCELLED);

        // 5. Menyimpan Perubahan Status ke Database
        appointmentRepository.save(appointment);

        //6. cancel payment
        paymentService.cancelPaymentForAppointment(appointmentId);

    }

    @Override
    public AppointmentResponse findById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Appointment not found with id: " + appointmentId));

        return convertToResponse(appointment);
    }

    @Override
    public List<AppointmentResponse> listDoctorAppointments(Long doctorId, LocalDate appointmentDate) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentDateOrderByStartTimeAsc(doctorId, appointmentDate);
        return appointments.stream()
                .map(this::convertToResponse)
                .toList();
    }

    private AppointmentResponse convertToResponse(Appointment appointment) {
        PaymentResponse paymentResponse = paymentService.findByAppointmentId(appointment.getId());
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .hospitalId(appointment.getHospitalId())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .consultationType(appointment.getConsultationType())
                .status(appointment.getStatus())
                .paymentDetails(paymentResponse)
                .build();
    }
}
