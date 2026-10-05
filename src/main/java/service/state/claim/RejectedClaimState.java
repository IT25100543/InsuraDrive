package com.insuradrive.service.state.claim;

import com.insuradrive.model.Claim;
import org.springframework.stereotype.Component;

@Component
public class RejectedClaimState implements ClaimState {

    @Override
    public Claim.ClaimStatus getStatus() {
        return Claim.ClaimStatus.REJECTED;
    }

    @Override
    public String getDescription() {
        return "Claim declined due to policy exclusion, fraudulent reporting, or lack of coverage.";
    }

    @Override
    public boolean canEditDetails() {
        return false;
    }

    @Override
    public void transitionTo(Claim claim, Claim.ClaimStatus targetStatus, Double approvedAmount, String remarks) {
        // Once rejected, reopening requires formal appeal
        claim.setStatus(targetStatus);
        if (remarks != null && !remarks.trim().isEmpty()) {
            claim.setOfficerRemarks(remarks.trim());
        }
    }
}
