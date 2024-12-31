package com.oneDev.healthcarebooking.repository;

import com.oneDev.healthcarebooking.entity.HospitalDoctorFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalDoctorFeeRepository extends JpaRepository<HospitalDoctorFee, Long> {
}
