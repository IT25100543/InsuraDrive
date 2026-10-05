package com.insuradrive.service.strategy;

import com.insuradrive.model.InsurancePolicy;
import org.springframework.stereotype.Component;

@Component
public class ThirdPartyFireTheftStrategy implements PremiumCalculationStrategy {

    @Override
    public InsurancePolicy.PolicyType getSupportedPolicyType() {
        return InsurancePolicy.PolicyType.THIRD_PARTY_FIRE_THEFT;
    }

    @Override
    public Double calculatePremium(Double estimatedValue, Integer engineCc, Integer manufactureYear) {
        if (estimatedValue == null || estimatedValue <= 0) {
            estimatedValue = 5000000.0;
        }

        double baseRate = 0.007; // 0.7%
        double premium = estimatedValue * baseRate;

        if (engineCc != null && engineCc > 1800) {
            premium += 10000.0;
        }

        return Math.round(premium * 100.0) / 100.0;
    }
}
