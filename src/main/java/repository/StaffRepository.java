package com.insuradrive.repository;

import com.insuradrive.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmployeeNo(String employeeNo);
    Optional<Staff> findByUserID(Long userID);
}
