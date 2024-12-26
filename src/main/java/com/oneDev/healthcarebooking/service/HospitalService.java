package com.oneDev.healthcarebooking.service;

import com.oneDev.healthcarebooking.model.request.HospitalRequest;
import com.oneDev.healthcarebooking.model.response.HospitalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HospitalService {

    Page<HospitalResponse> search(String name, Pageable pageable);
    HospitalResponse get(Long id);
    HospitalResponse update(Long id, HospitalRequest hospitalRequest);
    HospitalResponse create(HospitalRequest hospitalRequest);
    void delete(Long id);
}
