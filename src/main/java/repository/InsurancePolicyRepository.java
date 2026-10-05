package com.insuradrive.repository;

import com.insuradrive.model.InsurancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, Long> {

    Optional<InsurancePolicy> findByPolicyNumber(String policyNumber);

    @Query("SELECT p FROM InsurancePolicy p WHERE p.application.customer.userID = :customerId ORDER BY p.startDate DESC")
    List<InsurancePolicy> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT p FROM InsurancePolicy p WHERE p.application.vehicle.vehicleID = :vehicleId")
    List<InsurancePolicy> findByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT p FROM InsurancePolicy p WHERE UPPER(p.policyStatus) LIKE UPPER(CONCAT('%', :statusStr, '%'))")
    List<InsurancePolicy> findByPolicyStatusContainingIgnoreCase(@Param("statusStr") String statusStr);

    default List<InsurancePolicy> findByStatus(InsurancePolicy.PolicyStatus status) {
        if (status == null) return findAll();
        return findByPolicyStatusContainingIgnoreCase(status.name());
    }
}
