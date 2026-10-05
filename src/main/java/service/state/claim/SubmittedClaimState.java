package com.insuradrive.service.state.claim;

import com.insuradrive.model.Claim;
import org.springframework.stereotype.Component;

@Component
public class SubmittedClaimState implements ClaimState {

    @Override
    public Claim.ClaimStatus getStatus() {
        return Claim.ClaimStatus.SUBMITTED;
    }

    @Override
    public String getDescription() {
        return "Claim lodged by customer. Pending initial verification by Claims Desk.";
    }

    @Override
    public boolean canEditDetails() {
        return true;
    }

    @Override
    public void transitionTo(Claim claim, Claim.ClaimStatus targetStatus, Double approvedAmount, String remarks) {
        claim.setStatus(targetStatus);
        if (remarks != null && !remarks.trim().isEmpty()) {
            claim.setOfficerRemarks(remarks.trim());
        }
    }
}
