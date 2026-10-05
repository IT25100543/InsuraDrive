package com.insuradrive.service.state.claim;

import com.insuradrive.model.Claim;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UnderVerificationClaimState implements ClaimState {

    @Override
    public Claim.ClaimStatus getStatus() {
        return Claim.ClaimStatus.UNDER_VERIFICATION;
    }

    @Override
    public String getDescription() {
        return "Claim currently being evaluated by loss adjusters and physical assessors.";
    }

    @Override
    public boolean canEditDetails() {
        return false;
    }

    @Override
    public void transitionTo(Claim claim, Claim.ClaimStatus targetStatus, Double approvedAmount, String remarks) {
        claim.setStatus(targetStatus);
        if (remarks != null && !remarks.trim().isEmpty()) {
            claim.setOfficerRemarks(remarks.trim());
        }
        if (targetStatus == Claim.ClaimStatus.APPROVED) {
            claim.setApprovedAmount(approvedAmount != null ? approvedAmount : claim.getClaimedAmount());
            claim.setSettlementDate(LocalDate.now());
        } else if (targetStatus == Claim.ClaimStatus.REJECTED) {
            claim.setApprovedAmount(0.0);
        }
    }
}
