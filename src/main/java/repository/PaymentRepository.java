package com.insuradrive.repository;

import com.insuradrive.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPolicyId(Long policyId);

    @Query("SELECT p FROM Payment p WHERE p.policy.application.customer.userID = :customerId ORDER BY p.paymentDate DESC")
    List<Payment> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT p FROM Payment p WHERE UPPER(p.paymentStatus) = UPPER(:statusStr)")
    List<Payment> findByPaymentStatus(@Param("statusStr") String statusStr);

    default List<Payment> findByStatus(Payment.PaymentStatus status) {
        if (status == null) return findAll();
        String s = (status == Payment.PaymentStatus.COMPLETED) ? "Paid" : status.name();
        return findByPaymentStatus(s);
    }
}

