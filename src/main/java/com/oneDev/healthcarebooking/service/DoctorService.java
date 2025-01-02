package com.oneDev.healthcarebooking.service;

import com.oneDev.healthcarebooking.entity.Doctor;
import com.oneDev.healthcarebooking.entity.DoctorAvailability;
import com.oneDev.healthcarebooking.model.request.DoctorAvailabilityRequest;
import com.oneDev.healthcarebooking.model.request.DoctorRegistrationRequest;
import com.oneDev.healthcarebooking.model.response.DoctorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface DoctorService {
    DoctorResponse registerDoctor(DoctorRegistrationRequest request);
    Page<DoctorResponse> getAllDoctors(String keyword, Pageable pageable);
    DoctorResponse getDoctorById(Long doctorId);
    Doctor getDoctorByUserId(Long userId);
    DoctorResponse addDoctorSpecialization(Long doctorId, Long specializationId, BigDecimal fee, String consultationType);
    void deleteDoctorAvailability(Long doctorId, Long availabilityId);
    List<DoctorAvailability> getDoctorAvailabilityFromToday(Long doctorId);
    DoctorResponse updateDoctorAvailability(Long doctorId, DoctorAvailabilityRequest request);

}
