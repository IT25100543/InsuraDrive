package com.insuradrive.service.strategy.payment;

import com.insuradrive.model.Payment;
import com.insuradrive.model.InsurancePolicy;
import com.insuradrive.service.strategy.payment.PaymentProcessingStrategy;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Context class in the Payment Processing Strategy Design Pattern.
 * Selects and delegates transaction execution to the appropriate strategy
 * based on the selected payment method (Card, Bank Transfer, or Cash Desk).
 */
@Service
public class PaymentProcessingService {

    private final List<PaymentProcessingStrategy> strategies;
    private final com.insuradrive.service.strategy.payment.CardPaymentStrategy fallbackStrategy;

    public PaymentProcessingService(List<PaymentProcessingStrategy> strategies,
                                    com.insuradrive.service.strategy.payment.CardPaymentStrategy fallbackStrategy) {
        this.strategies = strategies;
        this.fallbackStrategy = fallbackStrategy;
    }

    public Payment executePayment(Payment payment, InsurancePolicy policy) {
        String method = payment.getPaymentMethod();
        PaymentProcessingStrategy selectedStrategy = strategies.stream()
                .filter(s -> s.supports(method))
                .findFirst()
                .orElse(fallbackStrategy);

        return selectedStrategy.processPayment(payment, policy);
    }
}

