package com.insuradrive.repository;

import com.insuradrive.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByPolicyId(Long policyId);

    @Query("SELECT c FROM Claim c WHERE c.policy.application.customer.userID = :customerId ORDER BY c.incidentDate DESC")
    List<Claim> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT c FROM Claim c WHERE UPPER(c.claimStatus) LIKE UPPER(CONCAT('%', :statusStr, '%'))")
    List<Claim> findByClaimStatusContainingIgnoreCase(@Param("statusStr") String statusStr);

    default List<Claim> findByStatus(Claim.ClaimStatus status) {
        if (status == null) return findAll();
        String s = "Submitted";
        if (status == Claim.ClaimStatus.APPROVED) s = "Approved";
        if (status == Claim.ClaimStatus.REJECTED) s = "Rejected";
        if (status == Claim.ClaimStatus.UNDER_VERIFICATION || status == Claim.ClaimStatus.DOCUMENTS_REQUIRED) s = "Review";
        if (status == Claim.ClaimStatus.SETTLED) s = "Settled";
        return findByClaimStatusContainingIgnoreCase(s);
    }
}
