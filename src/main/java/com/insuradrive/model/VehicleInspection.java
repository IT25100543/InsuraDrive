import jakarta.persistence.*;

import java.time.LocalDate;

public class VehicleInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inspectionID")
    private Integer inspectionID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicleID", nullable = false)
    private Vehicle vehicle;

    @Column(name = "inspectionDate", nullable = false)
    private LocalDate inspectionDate = LocalDate.now();

    @Column(name = "inspectionStatus", length = 30, nullable = false)
    private String inspectionStatus = "Pending";

    @Column(name = "result", length = 255)
    private String result;

    public VehicleInspection() {}

    public VehicleInspection(Integer inspectionID, Vehicle vehicle, LocalDate inspectionDate,
                             String inspectionStatus, String result) {
        this.inspectionID = inspectionID;
        this.vehicle = vehicle;
        this.inspectionDate = inspectionDate != null ? inspectionDate : LocalDate.now();
        this.inspectionStatus = inspectionStatus != null ? inspectionStatus : "Pending";
        this.result = result;
    }

    public static VehicleInspectionBuilder builder() {
        return new VehicleInspectionBuilder();
    }

    public static class VehicleInspectionBuilder {
        private Integer inspectionID;
        private Vehicle vehicle;
        private LocalDate inspectionDate = LocalDate.now();
        private String inspectionStatus = "Pending";
        private String result;

        public VehicleInspectionBuilder inspectionID(Integer inspectionID) { this.inspectionID = inspectionID; return this; }
        public VehicleInspectionBuilder vehicle(Vehicle vehicle) { this.vehicle = vehicle; return this; }
        public VehicleInspectionBuilder inspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; return this; }
        public VehicleInspectionBuilder inspectionStatus(String inspectionStatus) { this.inspectionStatus = inspectionStatus; return this; }
        public VehicleInspectionBuilder result(String result) { this.result = result; return this; }

        public VehicleInspection build() {
            return new VehicleInspection(inspectionID, vehicle, inspectionDate, inspectionStatus, result);
        }
    }

    public Integer getInspectionID() { return inspectionID; }
    public void setInspectionID(Integer inspectionID) { this.inspectionID = inspectionID; }

    public Integer getId() { return inspectionID; }
    public void setId(Integer id) { this.inspectionID = id; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public LocalDate getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }

    public String getInspectionStatus() { return inspectionStatus; }
    public void setInspectionStatus(String inspectionStatus) { this.inspectionStatus = inspectionStatus; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public boolean isPassed() {
        return "Completed".equalsIgnoreCase(inspectionStatus) &&
                (result != null && result.toLowerCase().contains("passed"));
    }
}

