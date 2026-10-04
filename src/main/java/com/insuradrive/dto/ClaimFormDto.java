package com.insuradrive.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class ClaimFormDto {

    private Long id;

    @NotNull(message = "Please select an active insurance policy")
    private Long policyId;

    @NotNull(message = "Incident date is required")
    @PastOrPresent(message = "Incident date cannot be in the future")
    private LocalDate incidentDate = LocalDate.now();

    @NotBlank(message = "Incident location is required")
    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String incidentLocation;

    @NotNull(message = "Claim amount is required")
    @Positive(message = "Claim amount must be a positive number greater than 0")
    private Double claimedAmount;

    @NotBlank(message = "Damage description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    private String documentAttachment = "police_report_verified.pdf";

    public ClaimFormDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }

    public LocalDate getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; }

    public String getIncidentLocation() { return incidentLocation; }
    public void setIncidentLocation(String incidentLocation) { this.incidentLocation = incidentLocation; }

    public Double getClaimedAmount() { return claimedAmount; }
    public void setClaimedAmount(Double claimedAmount) { this.claimedAmount = claimedAmount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDocumentAttachment() { return documentAttachment; }
    public void setDocumentAttachment(String documentAttachment) { this.documentAttachment = documentAttachment; }
}
