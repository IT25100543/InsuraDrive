package com.insuradrive.dto;

import jakarta.validation.constraints.*;

public class VehicleFormDto {

    private Long id;

    @NotNull(message = "Please select a registered customer")
    private Long customerId;

    @NotBlank(message = "Vehicle registration number is required")
    @Pattern(regexp = "^[A-Za-z0-9 -]{4,20}$", message = "Please enter a valid registration number (e.g. WP CAB-1234 or CAB-1234)")
    private String registrationNo;

    @NotBlank(message = "Make and model is required")
    @Size(min = 2, max = 50, message = "Make and model must be between 2 and 50 characters")
    private String model;

    @NotNull(message = "Manufacturer year is required")
    @Min(value = 1970, message = "Manufacture year must be 1970 or newer")
    @Max(value = 2027, message = "Manufacture year cannot be in the future")
    private Integer manufacturerYear;

    @NotNull(message = "Engine capacity (cc) is required")
    @Positive(message = "Engine capacity must be a positive number greater than 0")
    @Max(value = 10000, message = "Engine capacity cannot exceed 10,000 cc")
    private Integer engineCapacity;

    @NotBlank(message = "Chassis number is required")
    @Size(min = 5, max = 50, message = "Chassis number must be between 5 and 50 characters")
    private String chassisNo;

    private Double estimatedValue = 5000000.0;
    private String vehicleCategory = "Sedan / Standard";
    private String vehicleStatus = "Active";

    public VehicleFormDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getRegistrationNo() { return registrationNo; }
    public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getManufacturerYear() { return manufacturerYear; }
    public void setManufacturerYear(Integer manufacturerYear) { this.manufacturerYear = manufacturerYear; }

    public Integer getEngineCapacity() { return engineCapacity; }
    public void setEngineCapacity(Integer engineCapacity) { this.engineCapacity = engineCapacity; }

    public String getChassisNo() { return chassisNo; }
    public void setChassisNo(String chassisNo) { this.chassisNo = chassisNo; }

    public Double getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(Double estimatedValue) { this.estimatedValue = estimatedValue; }

    public String getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(String vehicleCategory) { this.vehicleCategory = vehicleCategory; }

    public String getVehicleStatus() { return vehicleStatus; }
    public void setVehicleStatus(String vehicleStatus) { this.vehicleStatus = vehicleStatus; }
}

