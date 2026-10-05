package com.insuradrive.service.state.claim;

import com.insuradrive.model.Claim;

/**
 * State Design Pattern: State Interface for Insurance Claims Lifecycle.
 * Solves regulatory compliance and business integrity rules:
 * - Claims must follow legal sequence: SUBMITTED -> UNDER_VERIFICATION -> APPROVED / REJECTED / DOCUMENTS_REQUIRED -> SETTLED
 * - Prevents illegal state transitions (e.g. paying/settling a rejected or unverified claim).
 */
public interface ClaimState {
    Claim.ClaimStatus getStatus();
    String getDescription();
    boolean canEditDetails();
    void transitionTo(Claim claim, Claim.ClaimStatus targetStatus, Double approvedAmount, String remarks);
}
