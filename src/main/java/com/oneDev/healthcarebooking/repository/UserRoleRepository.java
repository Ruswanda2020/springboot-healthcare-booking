package com.oneDev.healthcarebooking.repository;

import com.oneDev.healthcarebooking.entity.UserRole;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.UserRoleId> {

    @Modifying
    @Transactional
    @Query(value = """
       DELETE FROM user_role
       WHERE user_id = :userId
    """, nativeQuery = true)
    void deleteByIdUserId(@Param("userId") Long userId);

    @Query(value = """
       SELECT * FROM user_role
       WHERE user_id = :userId
       AND role_id = :roleId
       LIMIT 1
    """, nativeQuery = true)
    Optional<UserRole> existByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId")Long roleId);
}
