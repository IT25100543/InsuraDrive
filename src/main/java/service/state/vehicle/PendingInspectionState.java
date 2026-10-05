package com.insuradrive.service.state.vehicle;

import com.insuradrive.model.Vehicle;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PendingInspectionState implements com.insuradrive.service.state.vehicle.VehicleInspectionState {

    @Override
    public Vehicle.InspectionStatus getStatus() {
        return Vehicle.InspectionStatus.PENDING_INSPECTION;
    }

    @Override
    public boolean canIssuePolicy() {
        return false;
    }

    @Override
    public String getStatusDescription() {
        return "Mandatory physical roadworthiness inspection pending (> 10 years old or flagged). Policy issuance blocked.";
    }

    @Override
    public void applyInspection(Vehicle vehicle, Vehicle.InspectionStatus newStatus, String notes) {
        vehicle.setInspectionStatus(newStatus);
        vehicle.setInspectionNotes(notes);
        vehicle.setInspectionDate(LocalDate.now());
        if (newStatus == Vehicle.InspectionStatus.PASSED) {
            vehicle.setVehicleStatus("Active");
        } else if (newStatus == Vehicle.InspectionStatus.FAILED) {
            vehicle.setVehicleStatus("Inspection Failed");
        } else {
            vehicle.setVehicleStatus("Pending Inspection");
        }
    }
}