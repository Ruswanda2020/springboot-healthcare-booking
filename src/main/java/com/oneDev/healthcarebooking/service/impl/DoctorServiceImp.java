package com.oneDev.healthcarebooking.service.impl;

import com.oneDev.healthcarebooking.entity.*;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.enumaration.RoleType;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.AvailabilityInfo;
import com.oneDev.healthcarebooking.model.SpecializationInfo;
import com.oneDev.healthcarebooking.model.request.DoctorAvailabilityRequest;
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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImp implements DoctorService {

    // Dependencies for accessing repositories and services
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final HospitalRepository hospitalRepository;
    private final SpecializationRepository specializationRepository;
    private final DoctorSpecializationRepository doctorSpecializationRepository;
    private final HospitalDoctorFeeRepository hospitalDoctorFeeRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final CacheService cacheService;

    public static final String DOCTOR_CACHE_KEY = "cache:key:doctor";

    @Override
    @Transactional
    public DoctorResponse registerDoctor(DoctorRegistrationRequest request) {
        log.info("registerDoctor");

        // Fetch and validate the user requesting to be a doctor
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "User not found with id: " + request.getUserId()));

        // Fetch and validate the associated hospital
        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Hospital not found with id: " + request.getHospitalId()));

        // Fetch the doctor role from the role repository
        Role role = roleRepository.findByName(RoleType.DOCTOR)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Doctor role not found"));

        // Grant the doctor role to the user
        userService.grantUserRole(user.getUserId(), RoleType.DOCTOR);

        // Create and save a new doctor entity
        Doctor doctor = Doctor.builder()
                .userId(user.getUserId())
                .hospitalId(hospital.getId())
                .name(request.getName())
                .bio(request.getBio())
                .build();
        doctor = doctorRepository.save(doctor);

        // Process and save specializations associated with the doctor
        List<SpecializationInfo> specializationInfos = new ArrayList<>();
        for (DoctorSpecializationRequest specRequest : request.getSpecializations()) {
            // Validate specialization by ID
            Specialization specialization = specializationRepository.findById(specRequest.getSpecializationId())
                    .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                            "Specialization not found with id: " + specRequest.getSpecializationId()));

            // Create and save the doctor specialization
            DoctorSpecialization doctorSpecialization = DoctorSpecialization.builder()
                    .doctorId(doctor.getId())
                    .specializationId(specialization.getId())
                    .baseFee(specRequest.getBaseFee())
                    .consultationType(specRequest.getConsultationType())
                    .build();
            doctorSpecialization = doctorSpecializationRepository.save(doctorSpecialization);

            // Create and save hospital-specific fees for the specialization
            HospitalDoctorFee hospitalDoctorFee = HospitalDoctorFee.builder()
                    .hospitalId(hospital.getId())
                    .fee(specRequest.getBaseFee())
                    .consultationType(specRequest.getConsultationType())
                    .doctorSpecializationId(doctorSpecialization.getId())
                    .build();
            hospitalDoctorFee = hospitalDoctorFeeRepository.save(hospitalDoctorFee);

            // Add specialization info to the response
            specializationInfos.add(SpecializationInfo.builder()
                    .specializationId(specialization.getId())
                    .specializationName(specialization.getName())
                    .baseFee(doctorSpecialization.getBaseFee())
                    .hospitalFee(hospitalDoctorFee.getFee())
                    .consultationType(hospitalDoctorFee.getConsultationType())
                    .build());
        }

        log.info("Register doctor end with ID{}", doctor.getId());

        // Cache the doctor data for quick future access
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

        cacheService.put(cacheKey, doctorResponse, Duration.ofHours(1));
        return doctorResponse;
    }

    @Override
    public Page<DoctorResponse> getAllDoctors(String keyword, Pageable pageable) {
        // Search for doctors using a keyword and return a paginated result
        return doctorRepository.searchDoctors(keyword, pageable)
                .map(doctor -> getDoctorById(doctor.getId()));
    }

    @Override
    public DoctorResponse getDoctorById(Long doctorId) {
        // Check if the doctor data exists in the cache
        String cacheKey = DOCTOR_CACHE_KEY + doctorId;
        return cacheService.get(cacheKey, DoctorResponse.class)
                .orElseGet(() -> {
                    // Fetch doctor from database if not cached
                    Doctor doctor = doctorRepository.findById(doctorId)
                            .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                                    "Doctor not found with id: " + doctorId));
                    DoctorResponse doctorResponse = convertToDoctorResponse(doctor);

                    // Cache the fetched data for future use
                    cacheService.put(cacheKey, doctorResponse, Duration.ofHours(1));
                    return doctorResponse;
                });
    }

    @Override
    public Doctor getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Doctor not found with id: " + userId));

    }

    @Override
    @Transactional
    public DoctorResponse addDoctorSpecialization(
            Long doctorId,
            Long specializationId,
            BigDecimal fee,
            String consultationType) {

        // Validate the doctor and specialization existence
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Doctor not found with id: " + doctorId));

        Specialization specialization = specializationRepository.findById(specializationId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Specialization not found with id: " + specializationId));

        // Create and save doctor specialization
        DoctorSpecialization doctorSpecialization = DoctorSpecialization.builder()
                .doctorId(doctorId)
                .specializationId(specializationId)
                .consultationType(consultationType)
                .baseFee(fee)
                .build();
        doctorSpecialization = doctorSpecializationRepository.save(doctorSpecialization);

        // Create and save hospital doctor fee for the new specialization
        HospitalDoctorFee hospitalDoctorFee = HospitalDoctorFee.builder()
                .hospitalId(doctor.getHospitalId())
                .doctorSpecializationId(doctorSpecialization.getId())
                .fee(fee)
                .consultationType(consultationType)
                .build();
        hospitalDoctorFeeRepository.save(hospitalDoctorFee);

        // Evict cached data for this doctor to ensure freshness
        String cacheKey = DOCTOR_CACHE_KEY + doctor.getId();
        cacheService.evict(cacheKey);

        return convertToDoctorResponse(doctor);
    }

    @Override
    public void deleteDoctorAvailability(Long doctorId, Long availabilityId) {
        // Validate availability record
        DoctorAvailability doctorAvailability = doctorAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Doctor availability not found with id: " + availabilityId));

        // Verify the availability belongs to the specified doctor
        DoctorResponse doctorResponse = getDoctorById(doctorId);
        if (!doctorAvailability.getDoctorId().equals(doctorResponse.getId())) {
            throw new ApplicationException(ExceptionType.FORBIDDEN, "Cannot update doctor availability");
        }

        // Delete availability record and evict cache
        doctorAvailabilityRepository.delete(doctorAvailability);
        String cacheKey = DOCTOR_CACHE_KEY + doctorId;
        cacheService.evict(cacheKey);
    }

    @Override
    public List<DoctorAvailability> getDoctorAvailabilityFromToday(Long doctorId) {
        // Fetch doctor availability records from today onwards
        return doctorAvailabilityRepository.findAvailabilityByDoctorIdFromToday(doctorId);
    }

    @Override
    @Transactional
    public DoctorResponse updateDoctorAvailability(Long doctorId, DoctorAvailabilityRequest request) {
        // Validate doctor existence
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "Doctor not found with id: " + doctorId));

        // Check for duplicate availability (schedule conflict)
        boolean isDuplicate = doctorAvailabilityRepository.existsDuplicateAvailability(
                doctorId, request.getDate(), request.getStartTime(), request.getEndTime(), request.getConsultationType());

        if (isDuplicate) {
            throw new ApplicationException(ExceptionType.RESOURCE_CONFLICT,
                    "Doctor already has an availability scheduled at this time.");
        }

        // Create and save new doctor availability record
        DoctorAvailability doctorAvailability = DoctorAvailability.builder()
                .doctorId(doctorId)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .consultationType(request.getConsultationType())
                .isAvailable(true)
                .build();
        doctorAvailabilityRepository.save(doctorAvailability);

        // Evict cached doctor data to reflect updated availability
        String cacheKey = DOCTOR_CACHE_KEY + doctorId;
        cacheService.evict(cacheKey);

        return convertToDoctorResponse(doctor);
    }

    private DoctorResponse convertToDoctorResponse(Doctor doctor) {
        // Fetch user details for the doctor
        User user = userRepository.findById(doctor.getUserId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND,
                        "User not found with id: " + doctor.getUserId()));

        // Fetch hospital name or return a default value
        String hospitalName = hospitalRepository.findById(doctor.getHospitalId())
                .map(Hospital::getName)
                .orElse("unknown hospital name");

        // Fetch and map doctor specializations to response format
        List<DoctorSpecialization> specializations = doctorSpecializationRepository.findByDoctorId(doctor.getId());
        List<SpecializationInfo> specializationInfos = specializations.stream()
                .map(spec -> {
                    String specializationName = specializationRepository.findById(spec.getSpecializationId())
                            .map(Specialization::getName)
                            .orElse("unknown specialization name");

                    HospitalDoctorFee hospitalDoctorFee = hospitalDoctorFeeRepository.findByHospitalIdAndDoctorSpecializationId(doctor.getHospitalId(), spec.getId())
                            .orElse(new HospitalDoctorFee());

                    return SpecializationInfo.builder()
                            .specializationId(spec.getId())
                            .specializationName(specializationName)
                            .baseFee(hospitalDoctorFee.getFee())
                            .hospitalFee(hospitalDoctorFee.getFee())
                            .consultationType(hospitalDoctorFee.getConsultationType())
                            .build();
                }).toList();

        // Fetch doctor availability and map to response format
        List<AvailabilityInfo> availabilityInfos = getDoctorAvailabilityFromToday(doctor.getId())
                .stream()
                .map(doctorAvailability ->
                        AvailabilityInfo.builder()
                                .id(doctorAvailability.getId())
                                .isAvailable(true)
                                .startDateTime(
                                        LocalDateTime.of(
                                                doctorAvailability.getDate(),
                                                doctorAvailability.getStartTime()
                                        )
                                )
                                .endDateTime(
                                        LocalDateTime.of(
                                                doctorAvailability.getDate(),
                                                doctorAvailability.getEndTime()
                                        )
                                )
                                .consultationType(doctorAvailability.getConsultationType())
                                .build()
                )
                .toList();

        // Build and return the full doctor response
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
                .availabilities(availabilityInfos)
                .build();
    }
}


