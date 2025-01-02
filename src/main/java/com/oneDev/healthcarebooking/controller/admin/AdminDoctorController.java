package com.oneDev.healthcarebooking.controller.admin;

import com.oneDev.healthcarebooking.model.UserInfo;
import com.oneDev.healthcarebooking.model.request.DoctorAvailabilityRequest;
import com.oneDev.healthcarebooking.model.request.DoctorRegistrationRequest;
import com.oneDev.healthcarebooking.model.request.DoctorSpecializationRequest;
import com.oneDev.healthcarebooking.model.response.DoctorResponse;
import com.oneDev.healthcarebooking.service.DoctorService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/admin/doctors")
@RestController
@SecurityRequirement(name = "Bearer")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
public class AdminDoctorController {

    private final DoctorService doctorService;

    @PostMapping
    public ResponseEntity<DoctorResponse> registerDoctor(@Valid @RequestBody DoctorRegistrationRequest request){
        DoctorResponse doctorResponse = doctorService.registerDoctor(request);
        return ResponseEntity.ok(doctorResponse);
    }

    @PostMapping("/{doctorId}/specialization")
    public ResponseEntity<DoctorResponse> addDoctorSpecialization(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorSpecializationRequest request){

        DoctorResponse response = doctorService.addDoctorSpecialization(
                doctorId, request.getSpecializationId(),
                request.getBaseFee(), request.getConsultationType()
        );
        return ResponseEntity.ok(response);
    }


}
