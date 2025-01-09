package com.oneDev.healthcarebooking.controller;

import com.oneDev.healthcarebooking.model.UserInfo;
import com.oneDev.healthcarebooking.model.request.AppointmentRequest;
import com.oneDev.healthcarebooking.model.request.AppointmentRescheduleRequest;
import com.oneDev.healthcarebooking.model.response.AppointmentResponse;
import com.oneDev.healthcarebooking.service.AppointmentService;
import com.oneDev.healthcarebooking.utils.UserInfoHelper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/appointments")
@RestController
@SecurityRequirement(name = "Bearer")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserInfoHelper userInfoHelper;


    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentRequest appointment) {
        AppointmentResponse response = appointmentService.bookAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PutMapping("/reschedule/{appointmentId}")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable("appointmentId") Long appointmentId,
            @Valid @RequestBody AppointmentRescheduleRequest request) {
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        AppointmentResponse response = appointmentService.rescheduleAppointment(userInfo.getUser().getUserId(), appointmentId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/cancel/{appointmentId}")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@PathVariable("appointmentId") Long appointmentId) {
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        appointmentService.cancelAppointment(userInfo.getUser().getUserId(), appointmentId);
        AppointmentResponse response = appointmentService.findById(appointmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> findAppointments() {
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        List<AppointmentResponse> responses = appointmentService.listUserAppointments(userInfo.getUser().getUserId());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> getById(@PathVariable("appointmentId") Long appointmentId) {
        UserInfo userInfo = userInfoHelper.getCurrentUserInfo();
        AppointmentResponse response = appointmentService.findById(appointmentId);
        return ResponseEntity.ok(response);
    }
}
