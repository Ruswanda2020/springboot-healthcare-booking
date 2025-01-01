package com.oneDev.healthcarebooking.service.impl;

import com.oneDev.healthcarebooking.entity.*;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.enumaration.RoleType;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.SpecializationInfo;
import com.oneDev.healthcarebooking.model.request.DoctorRegistrationRequest;
import com.oneDev.healthcarebooking.model.request.DoctorSpecializationRequest;
import com.oneDev.healthcarebooking.model.response.DoctorResponse;
import com.oneDev.healthcarebooking.repository.*;
import com.oneDev.healthcarebooking.service.CacheService;
import com.oneDev.healthcarebooking.service.DoctorService;
import com.oneDev.healthcarebooking.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImp implements DoctorService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final HospitalRepository hospitalRepository;
    private final SpecializationRepository specializationRepository;
    private final DoctorSpecializationRepository doctorSpecializationRepository;
    private final HospitalDoctorFeeRepository hospitalDoctorFeeRepository;
    private final DoctorRepository doctorRepository;
    private final CacheService cacheService;

    public static final String DOCTOR_CACHE_KEY = "cache:key:doctor";

    @Override @Transactional
    public DoctorResponse registerDoctor(DoctorRegistrationRequest request) {
        log.info("registerDoctor");

        //Fetch and validate User
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()-> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "User not found with id: " + request.getUserId()));

        //Fetch and validate hospital
        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(()-> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Hospital not found with id: " + request.getHospitalId()));

        //Fetch doctor role
        Role role = roleRepository.findByName(RoleType.DOCTOR)
                .orElseThrow(()-> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Doctor role not found"));

        //Grant doctor role to user
        userService.grantUserRole(user.getUserId(), RoleType.DOCTOR);

        //Create and save doctor entity
        Doctor doctor = Doctor.builder()
                .userId(user.getUserId())
                .hospitalId(hospital.getId())
                .name(request.getName())
                .bio(request.getBio())
                .build();
        doctor = doctorRepository.save(doctor);

        //process specialization
        List<SpecializationInfo> specializationInfos = new ArrayList<>();
        for (DoctorSpecializationRequest specRequest : request.getSpecializations()) {
            Specialization specialization = specializationRepository.findById(specRequest.getSpecializationId())
                    .orElseThrow(()-> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                            "Specialization not found with id: " + specRequest.getSpecializationId()));

            DoctorSpecialization doctorSpecialization = DoctorSpecialization.builder()
                    .doctorId(doctor.getId())
                    .specializationId(specialization.getId())
                    .baseFee(specRequest.getBaseFee())
                    .build();
            doctorSpecialization = doctorSpecializationRepository.save(doctorSpecialization);

            HospitalDoctorFee hospitalDoctorFee = HospitalDoctorFee.builder()
                    .hospitalId(hospital.getId())
                    .fee(specRequest.getBaseFee())
                    .doctorSpecializationId(doctorSpecialization.getId())
                    .build();
            hospitalDoctorFee = hospitalDoctorFeeRepository.save(hospitalDoctorFee);

            specializationInfos.add(SpecializationInfo.builder()
                            .specializationId(specialization.getId())
                            .specializationName(specialization.getName())
                            .baseFee(doctorSpecialization.getBaseFee())
                            .hospitalFee(hospitalDoctorFee.getFee())
                    .build());
        }

        log.info("Register doctor end with ID{}", doctor.getId());
        String cacheKey = DOCTOR_CACHE_KEY + doctor.getId();

        DoctorResponse doctorResponse = DoctorResponse.builder()
                .id(doctor.getId())
                .userId(user.getUserId())
                .name(doctor.getName())
                .email(user.getEmail())
                .hospitalId(hospital.getId())
                .hospitalName(hospital.getName())
                .bio(doctor.getBio())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .specializations(specializationInfos)
                .availabilities(new ArrayList<>())
                .build();

        cacheService.put(cacheKey, doctorResponse);
        return doctorResponse;
    }

    @Override
    public Page<DoctorResponse> getAllDoctors(String keyword, Pageable pageable) {
        return doctorRepository.searchDoctors(keyword, pageable)
                .map(doctor -> getDoctorById(doctor.getId()));
    }

    @Override
    public DoctorResponse getDoctorById(Long doctorId) {
        String cacheKey = DOCTOR_CACHE_KEY + doctorId;
        return cacheService.get(cacheKey, DoctorResponse.class)
                .orElseGet(()-> {
                   Doctor doctor = doctorRepository.findById(doctorId)
                           .orElseThrow(()-> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                                   "Doctor not found with id: " + doctorId));
                    return convertToDoctorResponse(doctor);
                });

    }

    private DoctorResponse convertToDoctorResponse(Doctor doctor){
        User user = userRepository.findById(doctor.getUserId())
                .orElseThrow(()-> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "User not found with id: " + doctor.getUserId()));

        String hospitalName = hospitalRepository.findById(doctor.getHospitalId())
                .map(Hospital::getName)
                .orElse("unknown hospital name ");

        List<DoctorSpecialization> specializations = doctorSpecializationRepository.findByDoctorId(doctor.getId());
        List<SpecializationInfo> specializationInfos = specializations.stream()
                .map(spec -> {
                    String specializationName = specializationRepository.findById(spec.getSpecializationId())
                            .map(Specialization::getName)
                            .orElse("unknown specialization name ");

                    HospitalDoctorFee hospitalDoctorFee = hospitalDoctorFeeRepository.findByHospitalIdAndDoctorSpecializationId(doctor.getHospitalId(), spec.getId())
                            .orElse(new HospitalDoctorFee());

                    return SpecializationInfo.builder()
                            .specializationId(spec.getId())
                            .specializationName(specializationName)
                            .baseFee(hospitalDoctorFee.getFee())
                            .build();
                }).toList();

        return DoctorResponse.builder()
                .id(doctor.getId())
                .userId(user.getUserId())
                .name(doctor.getName())
                .email(user.getEmail())
                .hospitalId(doctor.getHospitalId())
                .hospitalName(hospitalName)
                .bio(doctor.getBio())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .specializations(specializationInfos)
                .build();
    }
}
