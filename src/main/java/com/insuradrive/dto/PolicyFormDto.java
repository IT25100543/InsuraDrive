package com.insuradrive.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class PolicyFormDto {

    private Long id;

    @NotNull(message = "Please select a registered customer")
    private Long customerId;

    @NotNull(message = "Please select an enrolled vehicle")
    private Long vehicleId;

    @NotNull(message = "Please select an insurance policy type")
    private String policyType = "COMPREHENSIVE_STANDARD";

    @NotNull(message = "Policy start date is required")
    private LocalDate startDate = LocalDate.now();

    @NotNull(message = "Policy expiry date is required")
    private LocalDate expiryDate = LocalDate.now().plusYears(1);

    private Double premiumAmount;
    private String status = "ACTIVE";

    public PolicyFormDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getPolicyType() { return policyType; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Double getPremiumAmount() { return premiumAmount; }
    public void setPremiumAmount(Double premiumAmount) { this.premiumAmount = premiumAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
