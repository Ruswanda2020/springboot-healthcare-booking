package com.oneDev.healthcarebooking.repository;

import com.oneDev.healthcarebooking.entity.Hospital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    Page<Hospital> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
