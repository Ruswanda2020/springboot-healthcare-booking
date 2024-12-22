package com.oneDev.healthcarebooking.repository;

import com.oneDev.healthcarebooking.entity.Role;
import com.oneDev.healthcarebooking.enumaration.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleType name);

    @Query(value = """
    SELECT r.*
    FROM roles r
    JOIN user_role ur ON r.role_id = ur.role_id
    JOIN users u ON ur.user_id = u.user_Id
    WHERE u.user_id = :userId
    """, nativeQuery = true)
    List<Role> findByUserId(@Param("userId") Long userId);
}
