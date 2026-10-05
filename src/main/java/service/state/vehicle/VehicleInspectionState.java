package com.insuradrive.service.state.vehicle;

import com.insuradrive.model.Vehicle;

/**
 
 * binding depends strictly on physical roadworthiness inspection outcomes.
 */
public interface VehicleInspectionState {
    Vehicle.InspectionStatus getStatus();
    boolean canIssuePolicy();
    String getStatusDescription();
    void applyInspection(Vehicle vehicle, Vehicle.InspectionStatus newStatus, String notes);
}
