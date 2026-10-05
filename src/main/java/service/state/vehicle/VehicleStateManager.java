package com.insuradrive.service.state.vehicle;

import com.insuradrive.model.Vehicle;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;


@Service
public class VehicleStateManager {

    private final Map<Vehicle.InspectionStatus, com.insuradrive.service.state.vehicle.VehicleInspectionState> stateMap = new EnumMap<>(Vehicle.InspectionStatus.class);

    public VehicleStateManager(com.insuradrive.service.state.vehicle.PassedInspectionState passedState,
                               com.insuradrive.service.state.vehicle.PendingInspectionState pendingState,
                               com.insuradrive.service.state.vehicle.FailedInspectionState failedState) {
        stateMap.put(Vehicle.InspectionStatus.PASSED, passedState);
        stateMap.put(Vehicle.InspectionStatus.PENDING_INSPECTION, pendingState);
        stateMap.put(Vehicle.InspectionStatus.FAILED, failedState);
    }

    public com.insuradrive.service.state.vehicle.VehicleInspectionState getState(Vehicle.InspectionStatus status) {
        if (status == null) {
            return stateMap.get(Vehicle.InspectionStatus.PASSED);
        }
        return stateMap.getOrDefault(status, stateMap.get(Vehicle.InspectionStatus.PASSED));
    }

    public boolean canIssuePolicy(Vehicle vehicle) {
        if (vehicle == null) return false;
        return getState(vehicle.getInspectionStatus()).canIssuePolicy();
    }

    public void evaluateInitialInspectionState(Vehicle vehicle) {
        int currentYear = LocalDate.now().getYear();
        Integer mYear = vehicle.getManufacturerYear();
        if (mYear != null && (currentYear - mYear > 10)) {
            vehicle.setInspectionStatus(Vehicle.InspectionStatus.PENDING_INSPECTION);
            vehicle.setInspectionNotes("Older vehicle (> 10 years old). Flagged for mandatory physical roadworthiness inspection.");
            vehicle.setVehicleStatus("Pending Inspection");
        } else {
            vehicle.setInspectionStatus(Vehicle.InspectionStatus.PASSED);
            vehicle.setVehicleStatus("Active");
        }
    }

    public void transitionInspection(Vehicle vehicle, Vehicle.InspectionStatus newStatus, String notes) {
        com.insuradrive.service.state.vehicle.VehicleInspectionState currentState = getState(vehicle.getInspectionStatus());
        currentState.applyInspection(vehicle, newStatus, notes);
    }
}
