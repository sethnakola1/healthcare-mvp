package com.healthcare.mvp.user.repository;

import com.healthcare.mvp.user.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, UUID> {

    List<Users> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
    Page<Users> findByHospitalIdAndRoleAndIsActiveTrue(UUID hospitalId, String role, Pageable pageable);
    boolean existsByUsernameAndHospitalIdAndIsActiveTrue(String username, UUID hospitalId);
    boolean existsByEmailAndHospitalIdAndIsActiveTrue(String email, UUID hospitalId);
    Users findByEmailAndHospitalIdAndIsActiveTrue(String email, UUID hospitalId);

    //    Optional<Users> findByHospitalIdAndIsActiveTrue(UUID hospitalI);
//    Page<Users> findByHospitalIdAndIsActiveTrue(UUID hospitalId, Pageable pageable);
}