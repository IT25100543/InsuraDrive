package com.insuradrive.service.state.claim;

import com.insuradrive.model.Claim;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ApprovedClaimState implements ClaimState {

    @Override
    public Claim.ClaimStatus getStatus() {
        return Claim.ClaimStatus.APPROVED;
    }

    @Override
    public String getDescription() {
        return "Claim approved for compensation payout. Awaiting finance settlement.";
    }

    @Override
    public boolean canEditDetails() {
        return false;
    }

    @Override
    public void transitionTo(Claim claim, Claim.ClaimStatus targetStatus, Double approvedAmount, String remarks) {
        if (targetStatus == Claim.ClaimStatus.SETTLED) {
            claim.setStatus(Claim.ClaimStatus.SETTLED);
            claim.setSettlementDate(LocalDate.now());
            if (remarks != null && !remarks.trim().isEmpty()) {
                claim.setOfficerRemarks(remarks.trim());
            }
        } else {
            claim.setStatus(targetStatus);
            if (remarks != null && !remarks.trim().isEmpty()) {
                claim.setOfficerRemarks(remarks.trim());
            }
        }
    }
}
