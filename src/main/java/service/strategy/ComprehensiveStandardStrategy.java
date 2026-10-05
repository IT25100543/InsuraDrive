package com.insuradrive.service.strategy;

import com.insuradrive.model.InsurancePolicy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ComprehensiveStandardStrategy implements PremiumCalculationStrategy {

    @Override
    public InsurancePolicy.PolicyType getSupportedPolicyType() {
        return InsurancePolicy.PolicyType.COMPREHENSIVE_STANDARD;
    }

    @Override
    public Double calculatePremium(Double estimatedValue, Integer engineCc, Integer manufactureYear) {
        if (estimatedValue == null || estimatedValue <= 0) {
            estimatedValue = 5000000.0;
        }

        double baseRate = 0.012; // 1.2%
        double premium = estimatedValue * baseRate;

        if (engineCc != null) {
            if (engineCc > 2500) premium += 20000.0;
            else if (engineCc > 1800) premium += 12000.0;
            else if (engineCc > 1500) premium += 6000.0;
        }

        if (manufactureYear != null) {
            int age = LocalDate.now().getYear() - manufactureYear;
            if (age > 10) premium *= 1.15;
            else if (age > 5) premium *= 1.08;
        }

        return Math.round(premium * 100.0) / 100.0;
    }
}
