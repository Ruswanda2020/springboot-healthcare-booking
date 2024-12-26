package com.oneDev.healthcarebooking.service.impl;

import com.oneDev.healthcarebooking.entity.Hospital;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.request.HospitalRequest;
import com.oneDev.healthcarebooking.model.response.HospitalResponse;
import com.oneDev.healthcarebooking.repository.HospitalRepository;
import com.oneDev.healthcarebooking.service.CacheService;
import com.oneDev.healthcarebooking.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final CacheService cacheService;
    private static final String HOSPITAL_CACHE_KEY = "hospital:";

    @Override
    public Page<HospitalResponse> search(String name, Pageable pageable) {
        return hospitalRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(HospitalResponse::fromHospital);
    }

    @Override
    public HospitalResponse get(Long id) {
        String key = HOSPITAL_CACHE_KEY + id;
        return cacheService.get(key, HospitalResponse.class)
                .orElseGet(() -> {
                    return hospitalRepository.findById(id)
                            .map(hospital -> {
                                //convert entity to response
                                HospitalResponse hospitalResponse = HospitalResponse.fromHospital(hospital);
                                //store response in cache
                                cacheService.put(key, hospitalResponse);
                                return hospitalResponse;
                            }).orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,

                                    "Hospital with id: " + id + " not found!"));
                });
    }

    @Override
    public HospitalResponse update(Long id, HospitalRequest hospitalRequest) {
        // Retrieve the existing hospital entity
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Hospital with id: " + id + " not found!"));

        // Update the entity with new values or keep the old ones if the request value is null or empty
        hospital.setName(hospitalRequest.getName() == null || hospitalRequest.getName().isEmpty() ?
                hospital.getName() : hospitalRequest.getName());
        hospital.setAddress(hospitalRequest.getAddress() == null || hospitalRequest.getAddress().isEmpty() ?
                hospital.getAddress() : hospitalRequest.getAddress());
        hospital.setEmail(hospitalRequest.getEmail() == null || hospitalRequest.getEmail().isEmpty() ?
                hospital.getEmail() : hospitalRequest.getEmail());
        hospital.setPhone(hospitalRequest.getPhone() == null || hospitalRequest.getPhone().isEmpty() ?
                hospital.getPhone() : hospitalRequest.getPhone());
        hospital.setDescription(hospitalRequest.getDescription() == null || hospitalRequest.getDescription().isEmpty() ?
                hospital.getDescription() : hospitalRequest.getDescription());


        // Save the updated entity to the repository
        hospital = hospitalRepository.save(hospital);

        // Update the cache
        String key = HOSPITAL_CACHE_KEY + id;
        HospitalResponse response = HospitalResponse.fromHospital(hospital);
        cacheService.put(key, response);

        // Return the updated response
        return response;
    }

    @Override
    public HospitalResponse create(HospitalRequest hospitalRequest) {

        Hospital hospital = Hospital.builder()
                .name(hospitalRequest.getName())
                .email(hospitalRequest.getEmail())
                .phone(hospitalRequest.getPhone())
                .address(hospitalRequest.getAddress())
                .description(hospitalRequest.getDescription())
                .build();

        hospital = hospitalRepository.save(hospital);

        return HospitalResponse.fromHospital(hospital);
    }

    @Override
    public void delete(Long id) {
        if (!hospitalRepository.existsById(id)) {
            throw new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                    "Hospital with id: " + id + " not found!");
        }

        // Delete from repository
        hospitalRepository.deleteById(id);
        // Remove from cache
        String key = HOSPITAL_CACHE_KEY + id;
        cacheService.evict(key);
    }
}
