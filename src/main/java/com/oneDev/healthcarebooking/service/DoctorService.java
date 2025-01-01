package com.oneDev.healthcarebooking.service;

import com.oneDev.healthcarebooking.model.request.DoctorRegistrationRequest;
import com.oneDev.healthcarebooking.model.response.DoctorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DoctorService {
    DoctorResponse registerDoctor(DoctorRegistrationRequest request);
    Page<DoctorResponse> getAllDoctors(String keyword, Pageable pageable);
    DoctorResponse getDoctorById(Long doctorId);

}
