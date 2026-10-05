package com.insuradrive.service;

import com.insuradrive.model.InsurancePolicy;
import com.insuradrive.service.strategy.PremiumCalculationStrategy;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class InsuranceCalculationService {

    private final Map<InsurancePolicy.PolicyType, PremiumCalculationStrategy> strategyMap = new EnumMap<>(InsurancePolicy.PolicyType.class);

    public InsuranceCalculationService(List<PremiumCalculationStrategy> strategies) {
        for (PremiumCalculationStrategy strategy : strategies) {
            strategyMap.put(strategy.getSupportedPolicyType(), strategy);
        }
    }

    /**
     * Executes the strategy algorithm matching the requested policy type.
     */
    public Double calculateAnnualPremium(InsurancePolicy.PolicyType policyType, Double estimatedValue, Integer engineCc, Integer manufactureYear) {
        PremiumCalculationStrategy strategy = strategyMap.get(policyType);
        if (strategy == null) {
            // Default fallback
            strategy = strategyMap.get(InsurancePolicy.PolicyType.THIRD_PARTY_ONLY);
        }
        return strategy.calculatePremium(estimatedValue, engineCc, manufactureYear);
    }
}
