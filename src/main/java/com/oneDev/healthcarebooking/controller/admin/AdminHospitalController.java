package com.oneDev.healthcarebooking.controller.admin;

import com.oneDev.healthcarebooking.model.request.HospitalRequest;
import com.oneDev.healthcarebooking.model.response.HospitalResponse;
import com.oneDev.healthcarebooking.service.HospitalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RequestMapping("/admin/hospital")
@RestController
@SecurityRequirement(name = "Bearer")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminHospitalController {

    private final HospitalService hospitalService;

    @PostMapping
    public ResponseEntity<HospitalResponse> createHospital(@Valid @RequestBody HospitalRequest hospitalRequest) {
        HospitalResponse response = hospitalService.create(hospitalRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HospitalResponse> updateHospital(@PathVariable Long id, @Valid @RequestBody HospitalRequest hospitalRequest) {
        HospitalResponse response = hospitalService.update(id, hospitalRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHospital(@PathVariable Long id) {
        hospitalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
