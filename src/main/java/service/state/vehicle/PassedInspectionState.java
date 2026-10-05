package com.insuradrive.service.state.vehicle;

import com.insuradrive.model.Vehicle;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PassedInspectionState implements com.insuradrive.service.state.vehicle.VehicleInspectionState {

    @Override
    public Vehicle.InspectionStatus getStatus() {
        return Vehicle.InspectionStatus.PASSED;
    }

    @Override
    public boolean canIssuePolicy() {
        return true;
    }

    @Override
    public String getStatusDescription() {
        return "Vehicle passed roadworthiness inspection. Fully eligible for insurance policy binding.";
    }

    @Override
    public void applyInspection(Vehicle vehicle, Vehicle.InspectionStatus newStatus, String notes) {
        vehicle.setInspectionStatus(newStatus);
        vehicle.setInspectionNotes(notes);
        vehicle.setInspectionDate(LocalDate.now());
        if (newStatus == Vehicle.InspectionStatus.FAILED) {
            vehicle.setVehicleStatus("Inspection Failed");
        } else if (newStatus == Vehicle.InspectionStatus.PENDING_INSPECTION) {
            vehicle.setVehicleStatus("Pending Inspection");
        } else {
            vehicle.setVehicleStatus("Active");
        }
    }
}
