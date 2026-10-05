package com.insuradrive.service.strategy.payment;

import com.insuradrive.model.InsurancePolicy;
import com.insuradrive.model.Payment;

/**
 * Strategy Design Pattern: Strategy Interface for Payment Processing Channels.
 * Solves diverse transaction execution workflows:
 * - Card payments: Instant simulated gateway auth, immediate COMPLETED status, auto-receipt.
 * - Bank transfers: Bank slip validation, PENDING until supervisor clearance.
 * - Cash payments: In-branch cashier desk processing, supervisor sign-off.
 */
public interface PaymentProcessingStrategy {
    boolean supports(String paymentMethod);
    Payment processPayment(Payment payment, InsurancePolicy policy);
    String generateReceiptNumber(Payment payment);
}
