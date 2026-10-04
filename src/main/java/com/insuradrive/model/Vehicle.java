import com.insuradrive.model.Customer;
import com.insuradrive.model.PolicyApplication;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Vehicle {

    public enum InspectionStatus {
        PASSED,
        PENDING_INSPECTION,
        FAILED
    }

    public enum VehicleCategory {
        SEDAN,
        SUV,
        HATCHBACK,
        VAN,
        MOTORCYCLE,
        COMMERCIAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicleID")
    private Long vehicleID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customerID", nullable = false)
    private Customer customer;

    @Column(name = "registrationNo", length = 30, nullable = false, unique = true)
    private String registrationNo;

    @Column(name = "vehicleStatus", length = 20, nullable = false)
    private String vehicleStatus = "Active";

    @Column(name = "manufacturerYear", nullable = false)
    private Integer manufacturerYear;

    @Column(name = "model", length = 50, nullable = false)
    private String model;

    @Column(name = "chassisNo", length = 50, nullable = false, unique = true)
    private String chassisNo;

    @Column(name = "engineCapacity", nullable = false)
    private Integer engineCapacity;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehicleInspection> inspections = new ArrayList<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PolicyApplication> applications = new ArrayList<>();

    @Transient
    private Double estimatedValue = 5000000.0;

    @Transient
    private String vehicleCategory = "Sedan / Standard";

    @Enumerated(EnumType.STRING)
    @Transient
    private InspectionStatus inspectionStatus = InspectionStatus.PASSED;

    @Transient
    private LocalDate inspectionDate = LocalDate.now();

    @Transient
    private String inspectionNotes;

    public Vehicle() {}

    public Vehicle(Long vehicleID, Customer customer, String registrationNo, String vehicleStatus,
                   Integer manufacturerYear, String model, String chassisNo, Integer engineCapacity) {
        this.vehicleID = vehicleID;
        this.customer = customer;
        this.registrationNo = registrationNo;
        this.vehicleStatus = vehicleStatus != null ? vehicleStatus : "Active";
        this.manufacturerYear = manufacturerYear;
        this.model = model;
        this.chassisNo = chassisNo;
        this.engineCapacity = engineCapacity;
    }

    public static VehicleBuilder builder() {
        return new VehicleBuilder();
    }

    public static class VehicleBuilder {
        private Long vehicleID;
        private Customer customer;
        private String registrationNo;
        private String vehicleStatus = "Active";
        private Integer manufacturerYear;
        private String model;
        private String chassisNo;
        private Integer engineCapacity;
        private Double estimatedValue = 5000000.0;
        private String vehicleCategory = "Sedan / Standard";
        private InspectionStatus inspectionStatus = InspectionStatus.PASSED;
        private LocalDate inspectionDate = LocalDate.now();
        private String inspectionNotes;

        public VehicleBuilder vehicleID(Long vehicleID) { this.vehicleID = vehicleID; return this; }
        public VehicleBuilder vehicleID(Integer vehicleID) { this.vehicleID = vehicleID != null ? vehicleID.longValue() : null; return this; }
        public VehicleBuilder id(Long id) { this.vehicleID = id; return this; }
        public VehicleBuilder id(Integer id) { this.vehicleID = id != null ? id.longValue() : null; return this; }
        public VehicleBuilder customer(Customer customer) { this.customer = customer; return this; }
        public VehicleBuilder registrationNo(String registrationNo) { this.registrationNo = registrationNo; return this; }
        public VehicleBuilder vehicleStatus(String vehicleStatus) { this.vehicleStatus = vehicleStatus; return this; }
        public VehicleBuilder manufacturerYear(Integer manufacturerYear) { this.manufacturerYear = manufacturerYear; return this; }
        public VehicleBuilder manufactureYear(Integer manufactureYear) { this.manufacturerYear = manufactureYear; return this; }
        public VehicleBuilder model(String model) { this.model = model; return this; }
        public VehicleBuilder makeModel(String makeModel) { this.model = makeModel; return this; }
        public VehicleBuilder chassisNo(String chassisNo) { this.chassisNo = chassisNo; return this; }
        public VehicleBuilder chassisNumber(String chassisNumber) { this.chassisNo = chassisNumber; return this; }
        public VehicleBuilder engineCapacity(Integer engineCapacity) { this.engineCapacity = engineCapacity; return this; }
        public VehicleBuilder engineCapacityCc(Integer engineCapacityCc) { this.engineCapacity = engineCapacityCc; return this; }
        public VehicleBuilder estimatedValue(Double estimatedValue) { this.estimatedValue = estimatedValue; return this; }
        public VehicleBuilder vehicleCategory(String vehicleCategory) { this.vehicleCategory = vehicleCategory; return this; }
        public VehicleBuilder vehicleCategory(VehicleCategory category) { this.vehicleCategory = category != null ? category.name() : "SEDAN"; return this; }
        public VehicleBuilder inspectionStatus(InspectionStatus inspectionStatus) { this.inspectionStatus = inspectionStatus; return this; }
        public VehicleBuilder inspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; return this; }
        public VehicleBuilder inspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; return this; }

        public Vehicle build() {
            Vehicle v = new Vehicle(vehicleID, customer, registrationNo, vehicleStatus, manufacturerYear, model, chassisNo, engineCapacity);
            v.setEstimatedValue(estimatedValue);
            v.setVehicleCategory(vehicleCategory);
            v.setInspectionStatus(inspectionStatus);
            v.setInspectionDate(inspectionDate);
            v.setInspectionNotes(inspectionNotes);
            return v;
        }
    }

    public Long getVehicleID() { return vehicleID; }
    public void setVehicleID(Long vehicleID) { this.vehicleID = vehicleID; }
    public void setVehicleID(Integer vehicleID) { this.vehicleID = vehicleID != null ? vehicleID.longValue() : null; }

    public Long getId() { return vehicleID; }
    public void setId(Long id) { this.vehicleID = id; }
    public void setId(Integer id) { this.vehicleID = id != null ? id.longValue() : null; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getRegistrationNo() { return registrationNo; }
    public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }

    public String getVehicleStatus() { return vehicleStatus; }
    public void setVehicleStatus(String vehicleStatus) { this.vehicleStatus = vehicleStatus; }

    public Integer getManufacturerYear() { return manufacturerYear; }
    public void setManufacturerYear(Integer manufacturerYear) { this.manufacturerYear = manufacturerYear; }

    public Integer getManufactureYear() { return manufacturerYear; }
    public void setManufactureYear(Integer manufactureYear) { this.manufacturerYear = manufactureYear; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getMakeModel() { return model; }
    public void setMakeModel(String makeModel) { this.model = makeModel; }

    public String getChassisNo() { return chassisNo; }
    public void setChassisNo(String chassisNo) { this.chassisNo = chassisNo; }

    public String getChassisNumber() { return chassisNo; }
    public void setChassisNumber(String chassisNumber) { this.chassisNo = chassisNumber; }

    public Integer getEngineCapacity() { return engineCapacity; }
    public void setEngineCapacity(Integer engineCapacity) { this.engineCapacity = engineCapacity; }

    public Integer getEngineCapacityCc() { return engineCapacity; }
    public void setEngineCapacityCc(Integer engineCapacityCc) { this.engineCapacity = engineCapacityCc; }

    public List<VehicleInspection> getInspections() { return inspections; }
    public void setInspections(List<VehicleInspection> inspections) { this.inspections = inspections; }

    public List<PolicyApplication> getApplications() { return applications; }
    public void setApplications(List<PolicyApplication> applications) { this.applications = applications; }

    public Double getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(Double estimatedValue) { this.estimatedValue = estimatedValue; }

    public String getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(String vehicleCategory) { this.vehicleCategory = vehicleCategory; }

    public InspectionStatus getInspectionStatus() {
        if (inspectionStatus != null) return inspectionStatus;
        if ("Pending Inspection".equalsIgnoreCase(vehicleStatus)) return InspectionStatus.PENDING_INSPECTION;
        if ("Inspection Failed".equalsIgnoreCase(vehicleStatus)) return InspectionStatus.FAILED;
        return InspectionStatus.PASSED;
    }

    public void setInspectionStatus(InspectionStatus status) {
        this.inspectionStatus = status;
        if (status == InspectionStatus.PENDING_INSPECTION) {
            this.vehicleStatus = "Pending Inspection";
        } else if (status == InspectionStatus.FAILED) {
            this.vehicleStatus = "Inspection Failed";
        } else {
            this.vehicleStatus = "Active";
        }
    }

    public LocalDate getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }

    public String getInspectionNotes() { return inspectionNotes; }
    public void setInspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; }

    public boolean isPendingInspection() {
        return getInspectionStatus() == InspectionStatus.PENDING_INSPECTION;
    }
}
