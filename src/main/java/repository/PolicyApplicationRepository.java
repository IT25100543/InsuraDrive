package com.insuradrive.repository;

import com.insuradrive.model.PolicyApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyApplicationRepository extends JpaRepository<PolicyApplication, Integer> {

    @Query("SELECT pa FROM PolicyApplication pa WHERE pa.customer.userID = :customerId ORDER BY pa.applicationDate DESC")
    List<PolicyApplication> findByCustomerId(@Param("customerId") Long customerId);
}
