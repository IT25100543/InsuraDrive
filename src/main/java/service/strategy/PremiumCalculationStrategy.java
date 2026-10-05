package com.insuradrive.service.strategy;

import com.insuradrive.model.InsurancePolicy;

public interface PremiumCalculationStrategy {

    /**
     * Identifies which policy type this strategy applies to.
     */
    InsurancePolicy.PolicyType getSupportedPolicyType();

    /**
     * Calculates the annual premium based on vehicle value, engine CC, and age.
     */
    Double calculatePremium(Double estimatedValue, Integer engineCc, Integer manufactureYear);
}
