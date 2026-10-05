package com.insuradrive.service.strategy.payment;

import com.insuradrive.model.InsurancePolicy;
import com.insuradrive.model.Payment;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Concrete Strategy for Over-The-Counter Cash Desk Payments.
 * Executed at regional branches; status set to COMPLETED upon cashier receipting.
 */
@Component
public class CashPaymentStrategy implements com.insuradrive.service.strategy.payment.PaymentProcessingStrategy {

    @Override
    public boolean supports(String paymentMethod) {
        if (paymentMethod == null) return false;
        String m = paymentMethod.toLowerCase();
        return m.contains("cash") || m.contains("counter") || m.contains("branch");
    }

    @Override
    public Payment processPayment(Payment payment, InsurancePolicy policy) {
        payment.setPolicy(policy);
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setSupervisorVerified(true);
        payment.setPaymentDate(LocalDate.now());
        payment.setReceiptNumber(generateReceiptNumber(payment));
        if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
            payment.setTransactionId("TXN-CSH-" + System.currentTimeMillis() % 1000000);
        }
        return payment;
    }

    @Override
    public String generateReceiptNumber(Payment payment) {
        return "REC-CSH-" + (int)(Math.random() * 9000 + 1000);
    }
}
