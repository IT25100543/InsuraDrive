package com.insuradrive.service.state.vehicle;

import com.insuradrive.model.Vehicle;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FailedInspectionState implements com.insuradrive.service.state.vehicle.VehicleInspectionState {

    @Override
    public Vehicle.InspectionStatus getStatus() {
        return Vehicle.InspectionStatus.FAILED;
    }

    @Override
    public boolean canIssuePolicy() {
        return false;
    }

    @Override
    public String getStatusDescription() {
        return "Vehicle failed safety/roadworthiness inspection. Policy issuance strictly blocked until defects are rectified.";
    }

    @Override
    public void applyInspection(Vehicle vehicle, Vehicle.InspectionStatus newStatus, String notes) {
        vehicle.setInspectionStatus(newStatus);
        vehicle.setInspectionNotes(notes);
        vehicle.setInspectionDate(LocalDate.now());
        if (newStatus == Vehicle.InspectionStatus.PASSED) {
            vehicle.setVehicleStatus("Active");
        } else if (newStatus == Vehicle.InspectionStatus.PENDING_INSPECTION) {
            vehicle.setVehicleStatus("Pending Inspection");
        } else {
            vehicle.setVehicleStatus("Inspection Failed");
        }
    }
}
