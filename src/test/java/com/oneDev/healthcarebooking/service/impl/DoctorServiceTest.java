package com.oneDev.healthcarebooking.service.impl;

import com.oneDev.healthcarebooking.entity.*;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.enumaration.RoleType;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.request.DoctorRegistrationRequest;
import com.oneDev.healthcarebooking.model.request.DoctorSpecializationRequest;
import com.oneDev.healthcarebooking.model.response.DoctorResponse;
import com.oneDev.healthcarebooking.repository.*;
import com.oneDev.healthcarebooking.service.CacheService;
import com.oneDev.healthcarebooking.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserService userService;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private SpecializationRepository specializationRepository;

    @Mock
    private DoctorSpecializationRepository doctorSpecializationRepository;

    @Mock
    private HospitalDoctorFeeRepository hospitalDoctorFeeRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private DoctorServiceImp doctorServiceImp;

    private DoctorResponse doctorResponse;
    private DoctorRegistrationRequest request;
    private User user;
    private Role doctorRole;
    private Hospital hospital;
    private Specialization specialization;
    private Doctor doctor;
    private DoctorSpecialization doctorSpecialization;

    @BeforeEach
    void setUp() {
        // Inisialisasi DoctorRegistrationRequest
        request = new DoctorRegistrationRequest();
        request.setHospitalId(1L);
        request.setUserId(1L);
        request.setBio("Doctor's bio");

        DoctorSpecializationRequest specializationRequest = new DoctorSpecializationRequest();
        specializationRequest.setSpecializationId(1L);
        specializationRequest.setBaseFee(new BigDecimal("100.00"));

        request.setSpecializations(Collections.singletonList(specializationRequest));

        // Inisialisasi User
        user = new User();
        user.setUserId(1L);
        user.setUsername("doctoruser");
        user.setEmail("doctor@example.com");

        // Inisialisasi Hospital
        hospital = new Hospital();
        hospital.setName("Test Hospital");

        // Inisialisasi Role
        doctorRole = new Role();
        doctorRole.setRoleId(1L);
        doctorRole.setName(RoleType.DOCTOR);

        // Inisialisasi Specialization
        specialization = new Specialization();
        specialization.setId(1L);
        specialization.setName("General Medicine");

        // Inisialisasi Doctor
        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUserId(1L);
        doctor.setHospitalId(1L);
        doctor.setName("Dr. Title");
        doctor.setCreatedAt(LocalDateTime.now());
        doctor.setUpdatedAt(LocalDateTime.now());

        doctorSpecialization = DoctorSpecialization.builder()
                .doctorId(doctor.getId())
                .specializationId(specialization.getId())
                .baseFee(BigDecimal.valueOf(100.00))
                .build();
    }

    @Test
    void registerDoctor_Success_Caching() {
        // Mocking dependencies
        when(userRepository.findById(request.getUserId())).thenReturn(java.util.Optional.of(user));
        when(hospitalRepository.findById(request.getHospitalId())).thenReturn(java.util.Optional.of(hospital));
        when(roleRepository.findByName(RoleType.DOCTOR)).thenReturn(java.util.Optional.of(doctorRole));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(specializationRepository.findById(request.getSpecializations().get(0).getSpecializationId()))
                .thenReturn(java.util.Optional.of(specialization));
        when(doctorSpecializationRepository.save(any(DoctorSpecialization.class))).thenReturn(doctorSpecialization);
        when(hospitalDoctorFeeRepository.save(any(HospitalDoctorFee.class))).thenReturn(new HospitalDoctorFee());

        // Call the service method
        DoctorResponse response = doctorServiceImp.registerDoctor(request);

        // Assertions
        assertNotNull(response);

        // Gunakan key yang sesuai dengan implementasi
        String expectedCacheKey = "cache:key:doctor" + doctor.getId();
        verify(cacheService, times(1)).put(eq(expectedCacheKey), eq(response), eq(Duration.ofHours(1)));

        // Verify interactions
        verify(userRepository, times(1)).findById(request.getUserId());
        verify(hospitalRepository, times(1)).findById(request.getHospitalId());
        verify(roleRepository, times(1)).findByName(RoleType.DOCTOR);
        verify(doctorRepository, times(1)).save(any(Doctor.class));
        verify(specializationRepository, times(1)).findById(request.getSpecializations().get(0).getSpecializationId());
        verify(doctorSpecializationRepository, times(1)).save(any(DoctorSpecialization.class));
        verify(hospitalDoctorFeeRepository, times(1)).save(any(HospitalDoctorFee.class));
    }



    @Test
    void registerDoctor_Failure_UserNotFound() {
        // Mocking behavior: User not found
        when(userRepository.findById(request.getUserId()))
                .thenReturn(java.util.Optional.empty());

        // Call the service method and expect an exception
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            doctorServiceImp.registerDoctor(request);
        });

        // Assertions
        assertNotNull(exception);
        assertEquals(ExceptionType.RESOURCE_NOT_FOUND, exception.getType());
        assertEquals("User not found with id: " + request.getUserId(), exception.getMessage());

        // Verify interactions
        verify(userRepository, times(1)).findById(request.getUserId());
        verify(hospitalRepository, never()).findById(any());
        verify(roleRepository, never()).findByName(any());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void registerDoctor_Failure_HospitalNotFound() {
        // Mocking behavior: User found, but hospital not found
        when(userRepository.findById(request.getUserId()))
                .thenReturn(java.util.Optional.of(user));
        when(hospitalRepository.findById(request.getHospitalId()))
                .thenReturn(java.util.Optional.empty());

        // Call the service method and expect an exception
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            doctorServiceImp.registerDoctor(request);
        });

        // Assertions
        assertNotNull(exception);
        assertEquals(ExceptionType.RESOURCE_NOT_FOUND, exception.getType());
        assertEquals("Hospital not found with id: " + request.getHospitalId(), exception.getMessage());

        // Verify interactions
        verify(userRepository, times(1)).findById(request.getUserId());
        verify(hospitalRepository, times(1)).findById(request.getHospitalId());
        verify(roleRepository, never()).findByName(any());
        verify(doctorRepository, never()).save(any());
    }


}
