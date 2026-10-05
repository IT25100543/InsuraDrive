package com.insuradrive.service.state.claim;

import com.insuradrive.model.Claim;
import org.springframework.stereotype.Component;

@Component
public class SettledClaimState implements ClaimState {

    @Override
    public Claim.ClaimStatus getStatus() {
        return Claim.ClaimStatus.SETTLED;
    }

    @Override
    public String getDescription() {
        return "Claim fully settled and compensation disbursement completed to policyholder.";
    }

    @Override
    public boolean canEditDetails() {
        return false;
    }

    @Override
    public void transitionTo(Claim claim, Claim.ClaimStatus targetStatus, Double approvedAmount, String remarks) {
        // Settled claims are immutable terminal state
        if (remarks != null && !remarks.trim().isEmpty()) {
            claim.setOfficerRemarks(claim.getOfficerRemarks() + " | " + remarks.trim());
        }
    }
}
