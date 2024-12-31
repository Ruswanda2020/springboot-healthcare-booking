package com.oneDev.healthcarebooking.repository;

import com.oneDev.healthcarebooking.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {
    Optional<Specialization> findByNameContainingIgnoreCase(String name);
}
