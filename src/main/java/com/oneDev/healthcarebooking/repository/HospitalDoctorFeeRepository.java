package com.oneDev.healthcarebooking.repository;

import com.oneDev.healthcarebooking.entity.HospitalDoctorFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalDoctorFeeRepository extends JpaRepository<HospitalDoctorFee, Long> {
    Optional<HospitalDoctorFee> findByHospitalIdAndDoctorSpecializationId(Long hospitalId, Long doctorId);
}
