package com.insuradrive.service.strategy;

import com.insuradrive.model.InsurancePolicy;
import org.springframework.stereotype.Component;

@Component
public class ThirdPartyOnlyStrategy implements PremiumCalculationStrategy {

    @Override
    public InsurancePolicy.PolicyType getSupportedPolicyType() {
        return InsurancePolicy.PolicyType.THIRD_PARTY_ONLY;
    }

    @Override
    public Double calculatePremium(Double estimatedValue, Integer engineCc, Integer manufactureYear) {
        if (estimatedValue == null || estimatedValue <= 0) {
            estimatedValue = 5000000.0;
        }

        double baseRate = 0.0035; // 0.35%
        double premium = estimatedValue * baseRate;

        return Math.round(premium * 100.0) / 100.0;
    }
}
