package com.insuradrive.service.strategy.payment;

import com.insuradrive.model.InsurancePolicy;
import com.insuradrive.model.Payment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BankTransferPaymentStrategy implements com.insuradrive.service.strategy.payment.PaymentProcessingStrategy {

    @Override
    public boolean supports(String paymentMethod) {
        if (paymentMethod == null) return false;
        String m = paymentMethod.toLowerCase();
        return m.contains("bank") || m.contains("transfer") || m.contains("deposit") || m.contains("wire");
    }

    @Override
    public Payment processPayment(Payment payment, InsurancePolicy policy) {
        payment.setPolicy(policy);
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setSupervisorVerified(true);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setReceiptNumber(generateReceiptNumber(payment));
        if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
            payment.setTransactionId("TXN-BNK-" + System.currentTimeMillis() % 1000000);
        }
        return payment;
    }

    @Override
    public String generateReceiptNumber(Payment payment) {
        return "RCP-BNK-" + (int)(Math.random() * 9000 + 1000);
    }
}
