package com.insuradrive.service.state.claim;

import com.insuradrive.model.Claim;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

/**
 * Context manager for Claim Lifecycle State Pattern.
 * Manages valid workflow transitions and enforces underwriting integrity.
 */
@Service
public class ClaimWorkflowService {

    private final Map<Claim.ClaimStatus, ClaimState> stateMap = new EnumMap<>(Claim.ClaimStatus.class);

    public ClaimWorkflowService(SubmittedClaimState submittedState,
                                UnderVerificationClaimState underVerificationState,
                                DocumentsRequiredClaimState docsRequiredState,
                                ApprovedClaimState approvedState,
                                RejectedClaimState rejectedState,
                                SettledClaimState settledState) {
        stateMap.put(Claim.ClaimStatus.SUBMITTED, submittedState);
        stateMap.put(Claim.ClaimStatus.UNDER_VERIFICATION, underVerificationState);
        stateMap.put(Claim.ClaimStatus.DOCUMENTS_REQUIRED, docsRequiredState);
        stateMap.put(Claim.ClaimStatus.APPROVED, approvedState);
        stateMap.put(Claim.ClaimStatus.REJECTED, rejectedState);
        stateMap.put(Claim.ClaimStatus.SETTLED, settledState);
    }

    public ClaimState getState(Claim.ClaimStatus status) {
        if (status == null) {
            return stateMap.get(Claim.ClaimStatus.SUBMITTED);
        }
        return stateMap.getOrDefault(status, stateMap.get(Claim.ClaimStatus.SUBMITTED));
    }

    public void transition(Claim claim, Claim.ClaimStatus targetStatus, Double approvedAmount, String remarks) {
        ClaimState currentState = getState(claim.getStatus());
        currentState.transitionTo(claim, targetStatus, approvedAmount, remarks);
    }

    public boolean canEdit(Claim claim) {
        if (claim == null) return false;
        return getState(claim.getStatus()).canEditDetails();
    }

    public String getStatusDescription(Claim claim) {
        if (claim == null) return "";
        return getState(claim.getStatus()).getDescription();
    }
}
