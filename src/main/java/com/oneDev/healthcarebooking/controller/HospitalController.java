package com.oneDev.healthcarebooking.controller;

import com.oneDev.healthcarebooking.model.response.HospitalResponse;
import com.oneDev.healthcarebooking.service.HospitalService;
import com.oneDev.healthcarebooking.utils.PageUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/hospitals")
@RestController
@SecurityRequirement(name = "Bearer")
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping
    public ResponseEntity<Page<HospitalResponse>> search(
            @RequestParam(required = false, defaultValue = "name") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String[] sort){

        // Parse sort parameters using PageUtil
        Sort sortOrder = Sort.by(PageUtil.parsSortOrderRequest(sort));
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // Call service layer
        Page<HospitalResponse> hospitals = hospitalService.search(keyword, pageable);
        return ResponseEntity.ok(hospitals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HospitalResponse> getHospitalById(@PathVariable Long id) {
        HospitalResponse response = hospitalService.get(id);
        return ResponseEntity.ok(response);
    }
}
