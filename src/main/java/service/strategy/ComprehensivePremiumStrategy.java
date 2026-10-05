package com.insuradrive.service.strategy;

import com.insuradrive.model.InsurancePolicy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ComprehensivePremiumStrategy implements PremiumCalculationStrategy {

    @Override
    public InsurancePolicy.PolicyType getSupportedPolicyType() {
        return InsurancePolicy.PolicyType.COMPREHENSIVE_PREMIUM;
    }

    @Override
    public Double calculatePremium(Double estimatedValue, Integer engineCc, Integer manufactureYear) {
        if (estimatedValue == null || estimatedValue <= 0) {
            estimatedValue = 5000000.0;
        }

        double baseRate = 0.015; // 1.5%
        double premium = estimatedValue * baseRate;

        // Engine CC Surcharges
        if (engineCc != null) {
            if (engineCc > 2500) premium += 25000.0;
            else if (engineCc > 1800) premium += 15000.0;
            else if (engineCc > 1500) premium += 8000.0;
        }

        // Vehicle Age Surcharge
        if (manufactureYear != null) {
            int age = LocalDate.now().getYear() - manufactureYear;
            if (age > 10) {
                premium *= 1.20; // 20% surcharge
            } else if (age > 5) {
                premium *= 1.10; // 10% surcharge
            }
        }

        return Math.round(premium * 100.0) / 100.0;
    }
}
