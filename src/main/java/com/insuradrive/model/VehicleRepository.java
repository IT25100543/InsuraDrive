package com.insuradrive.repository;

import com.insuradrive.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByRegistrationNo(String registrationNo);
    boolean existsByRegistrationNo(String registrationNo);

    List<Vehicle> findByVehicleStatusContainingIgnoreCase(String status);
    List<Vehicle> findByVehicleStatusIgnoreCase(String status);

    default List<Vehicle> findByInspectionStatus(Vehicle.InspectionStatus status) {
        if (status == null) {
            return findAll();
        }
        if (status == Vehicle.InspectionStatus.PENDING_INSPECTION) {
            return findByVehicleStatusContainingIgnoreCase("pending");
        } else if (status == Vehicle.InspectionStatus.FAILED) {
            return findByVehicleStatusContainingIgnoreCase("fail");
        } else {
            return findByVehicleStatusIgnoreCase("active");
        }
    }

    @Query("SELECT v FROM Vehicle v WHERE v.customer.userID = :customerId")
    List<Vehicle> findByCustomerId(@Param("customerId") Long customerId);
}
