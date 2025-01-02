package com.oneDev.healthcarebooking.repository;

import com.oneDev.healthcarebooking.entity.DoctorSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorSpecializationRepository extends JpaRepository<DoctorSpecialization, Long> {
    List<DoctorSpecialization> findByDoctorId(Long doctorId);
    List<DoctorSpecialization> findBySpecializationId(Long specializationId);
    Optional<DoctorSpecialization> findByDoctorIdAndSpecializationId(Long doctorId, Long specializationId);
    boolean existsByDoctorIdAndSpecializationIdAndConsultationType(Long doctorId, Long specializationId, String consultationType);

}
