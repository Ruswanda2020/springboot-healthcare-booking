package com.oneDev.healthcarebooking.controller;

import com.oneDev.healthcarebooking.entity.Doctor;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.UserInfo;
import com.oneDev.healthcarebooking.model.request.DoctorAvailabilityRequest;
import com.oneDev.healthcarebooking.model.response.DoctorResponse;
import com.oneDev.healthcarebooking.service.DoctorService;
import com.oneDev.healthcarebooking.utils.PageUtil;
import com.oneDev.healthcarebooking.utils.UserInfoHelper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/doctor")
@RestController
@SecurityRequirement(name = "Bearer")
public class DoctorController {

    private final DoctorService doctorService;
    private final UserInfoHelper userInfoHelper;


    @GetMapping
    public ResponseEntity<Page<DoctorResponse>> search(
            @RequestParam(required = false, defaultValue = "name") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String[] sort){

        // Parse sort parameters using PageUtil
        Sort sortOrder = Sort.by(PageUtil.parsSortOrderRequest(sort));
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<DoctorResponse> doctors = doctorService.getAllDoctors(keyword, pageable);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable("id") Long id) {
        DoctorResponse doctorResponse = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctorResponse);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/{doctorId}/availabilities")
    public ResponseEntity<DoctorResponse> createAvailability(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorAvailabilityRequest request
    ){
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        Doctor existingDoctor = doctorService.getDoctorByUserId(userInfo.getUser().getUserId());

        if (!existingDoctor.getUserId().equals(userInfo.getUser().getUserId())) {
            throw new ApplicationException(ExceptionType.FORBIDDEN, "Cannot update availability of doctor");
        }

        DoctorResponse doctorResponse = doctorService.updateDoctorAvailability(doctorId, request);
        return ResponseEntity.ok(doctorResponse);

    }

    @PreAuthorize("hasRole('DOCTOR')")
    @DeleteMapping("/availabilities/{availabilitiesId}")
    public ResponseEntity<DoctorResponse> deleteAvailability(@PathVariable Long availabilitiesId){
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        Doctor existingDoctor = doctorService.getDoctorByUserId(userInfo.getUser().getUserId());

        doctorService.deleteDoctorAvailability(existingDoctor.getId(), availabilitiesId);
        return ResponseEntity.noContent().build();

    }

}

