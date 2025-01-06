package com.oneDev.healthcarebooking.service.impl;

import com.oneDev.healthcarebooking.entity.*;
import com.oneDev.healthcarebooking.enumaration.AppointmentStatus;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.enumaration.PaymentStatus;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.request.AppointmentRequest;
import com.oneDev.healthcarebooking.model.response.AppointmentResponse;
import com.oneDev.healthcarebooking.model.response.PaymentResponse;
import com.oneDev.healthcarebooking.repository.*;
import com.oneDev.healthcarebooking.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HospitalDoctorFeeRepository hospitalDoctorFeeRepository;

    @Mock
    private DoctorSpecializationRepository doctorSpecializationRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private AppointmentRequest request;
    private User user;
    private Appointment appointment;
    private Hospital hospital;
    private Doctor doctor;
    private HospitalDoctorFee fee;
    private DoctorSpecialization doctorSpecialization;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        request = AppointmentRequest.builder()
                .userId(1L)
                .doctorSpecializationId(1L)
                .doctorId(1L)
                .appointmentDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .build();

        user = new User();
        user.setUserId(1L);
        user.setUsername("Patient");
        user.setEmail("johndoe@example.com");

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Dr. Smith");
        doctor.setHospitalId(1L);

        hospital = new Hospital();
        hospital.setId(1L);
        hospital.setName("City Hospital");

        fee = new HospitalDoctorFee();
        fee.setConsultationType("ONLINE");

        doctorSpecialization = new DoctorSpecialization();
        doctorSpecialization.setId(1L);

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        paymentResponse = PaymentResponse.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Test
    void bookAppointment_success() {
        // Arrange
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(user));
        when(doctorRepository.findById(request.getDoctorId())).thenReturn(Optional.of(doctor));
        when(hospitalRepository.findById(doctor.getHospitalId())).thenReturn(Optional.of(hospital));
        when(doctorSpecializationRepository.findById(request.getDoctorSpecializationId()))
                .thenReturn(Optional.of(doctorSpecialization));
        when(hospitalDoctorFeeRepository.findByHospitalIdAndDoctorSpecializationId(doctor.getHospitalId(), doctorSpecialization.getId()))
                .thenReturn(Optional.of(fee));
        when(doctorAvailabilityRepository.isDoctorAvailable(any(), any(), any(), any(), any())).thenReturn(true);
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment savedAppointment = invocation.getArgument(0);
            savedAppointment.setId(1L);
            return savedAppointment;
        });

        when(paymentService.createPayment(any(Appointment.class))).thenReturn(paymentResponse);

        // Act
        AppointmentResponse response = appointmentService.bookAppointment(request);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(appointment.getId(), response.getId(), "Appointment ID should match");
        assertEquals(hospital.getId(), response.getHospitalId(), "Hospital ID should match");
        assertEquals(doctor.getId(), response.getDoctorId(), "Doctor ID should match");
        assertEquals(AppointmentStatus.PENDING, response.getStatus(), "Appointment status should be PENDING");
        assertEquals(paymentResponse.getAmount(), response.getPaymentDetails().getAmount(), "Payment amount should match");
        assertEquals(paymentResponse.getStatus(), response.getPaymentDetails().getStatus(), "Payment status should match");

        // Verify interactions
        verify(userRepository).findById(request.getUserId());
        verify(doctorRepository).findById(request.getDoctorId());
        verify(hospitalRepository).findById(doctor.getHospitalId());
        verify(doctorSpecializationRepository).findById(request.getDoctorSpecializationId());
        verify(hospitalDoctorFeeRepository).findByHospitalIdAndDoctorSpecializationId(doctor.getHospitalId(), doctorSpecialization.getId());
        verify(doctorAvailabilityRepository).isDoctorAvailable(any(), any(), any(), any(), any());
        verify(appointmentRepository).save(any(Appointment.class));
        verify(paymentService).createPayment(any(Appointment.class));
    }

    @Test
    void bookAppointment_userNotFound() {
        // Arrange
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ApplicationException.class,
                () -> appointmentService.bookAppointment(request),
                "Expected exception when user not found");

        assertEquals("User not found with id: 1", exception.getMessage()); // Update expected message
        verify(userRepository).findById(request.getUserId());
        verifyNoMoreInteractions(doctorRepository, hospitalRepository, appointmentRepository, paymentService);
    }

    @Test
    void bookAppointment_doctorNotFound() {
        // Arrange
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(user));
        when(doctorRepository.findById(request.getDoctorId())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ApplicationException.class,
                () -> appointmentService.bookAppointment(request),
                "Expected exception when doctor not found");

        assertEquals("Doctor not found with id: 1", exception.getMessage());
        verify(userRepository).findById(request.getUserId());
        verify(doctorRepository).findById(request.getDoctorId());
        verifyNoMoreInteractions(hospitalRepository, appointmentRepository, paymentService);
    }

    @Test
    void bookAppointment_doctorUnavailable() {
        // Arrange
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(user));
        when(doctorRepository.findById(request.getDoctorId())).thenReturn(Optional.of(doctor));
        when(hospitalRepository.findById(doctor.getHospitalId())).thenReturn(Optional.of(hospital));
        when(doctorSpecializationRepository.findById(request.getDoctorSpecializationId()))
                .thenReturn(Optional.of(doctorSpecialization));
        when(hospitalDoctorFeeRepository.findByHospitalIdAndDoctorSpecializationId(doctor.getHospitalId(), doctorSpecialization.getId()))
                .thenReturn(Optional.of(fee));
        when(doctorAvailabilityRepository.isDoctorAvailable(any(), any(), any(), any(), any())).thenReturn(false);

        // Act & Assert
        assertThrows(ApplicationException.class, () -> appointmentService.bookAppointment(request)); // Corrected line
        verify(doctorAvailabilityRepository).isDoctorAvailable(any(), any(), any(), any(), any());
        verifyNoMoreInteractions(appointmentRepository, paymentService);
    }

    @Test
    void bookAppointment_timeAlreadyBooked() {
        // Arrange
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(user));
        when(doctorRepository.findById(request.getDoctorId())).thenReturn(Optional.of(doctor));
        when(hospitalRepository.findById(doctor.getHospitalId())).thenReturn(Optional.of(hospital));
        when(hospitalDoctorFeeRepository.findByHospitalIdAndDoctorSpecializationId(doctor.getHospitalId(), doctorSpecialization.getId()))
                .thenReturn(Optional.of(fee));
        when(doctorSpecializationRepository.findById(request.getDoctorSpecializationId()))
                .thenReturn(Optional.of(doctorSpecialization));
        when(doctorAvailabilityRepository.isDoctorAvailable(any(), any(), any(), any(), any())).thenReturn(true);

        // Menyimulasikan adanya jadwal appointment yang tumpang tindih
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(new Appointment()));

        assertThrows(ApplicationException.class, () -> appointmentService.bookAppointment(request));
    }

    @Test
    void bookAppointment_doctorSpecializationNotFound() {
        // Arrange
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(user));
        when(doctorRepository.findById(request.getDoctorId())).thenReturn(Optional.of(doctor));
        when(hospitalRepository.findById(doctor.getHospitalId())).thenReturn(Optional.of(hospital));
        when(doctorSpecializationRepository.findById(request.getDoctorSpecializationId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> appointmentService.bookAppointment(request),
                "Expected exception when doctor specialization is not found");

        // Memastikan pesan exception yang dilempar adalah "Doctor specialize not found"
        assertEquals("Doctor specialize id not found with id: " + request.getDoctorSpecializationId(), exception.getMessage());
    }


    @Test
    void bookAppointment_feeNotFound() {
        // Arrange
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(user));
        when(doctorRepository.findById(request.getDoctorId())).thenReturn(Optional.of(doctor));
        when(hospitalRepository.findById(doctor.getHospitalId())).thenReturn(Optional.of(hospital));
        when(doctorSpecializationRepository.findById(request.getDoctorSpecializationId()))
                .thenReturn(Optional.of(doctorSpecialization));
        // Fee tidak ditemukan untuk dokter dan spesialisasi yang diminta
        when(hospitalDoctorFeeRepository.findByHospitalIdAndDoctorSpecializationId(doctor.getHospitalId(), doctorSpecialization.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ApplicationException.class,
                () -> appointmentService.bookAppointment(request),
                "Expected exception when consultation fee not found");

        assertEquals("Doctor specialize not found", exception.getMessage());
        verify(hospitalDoctorFeeRepository).findByHospitalIdAndDoctorSpecializationId(doctor.getHospitalId(), doctorSpecialization.getId());
        verifyNoMoreInteractions(appointmentRepository, paymentService);
    }


}
