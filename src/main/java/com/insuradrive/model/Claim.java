package com.insuradrive.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CLAIM")
public class Claim {

    public enum ClaimStatus {
        SUBMITTED,
        UNDER_VERIFICATION,
        DOCUMENTS_REQUIRED,
        APPROVED,
        REJECTED,
        SETTLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "claimID")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policyID", nullable = false)
    private InsurancePolicy policy;

    @Column(name = "incidentDate", nullable = false)
    private LocalDate incidentDate = LocalDate.now();

    @Column(name = "submissionDate", nullable = false)
    private LocalDate submissionDate = LocalDate.now();

    @Column(name = "claimStatus", length = 30, nullable = false)
    private String claimStatus = "Submitted";

    @Column(name = "decision", length = 30)
    private String decision;

    @Column(name = "estimatedDamage", precision = 12, scale = 2)
    private BigDecimal estimatedDamage;

    @Column(name = "incidentLocation", length = 150, nullable = false)
    private String incidentLocation = "Colombo";

    @Column(name = "description", length = 500)
    private String description;

    @Transient
    private String claimNumber;

    @Transient
    private Double approvedAmount;

    @Transient
    private String documentName = "police_report_verified.pdf";

    @Transient
    private String officerRemarks;

    @Transient
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Transient
    private LocalDate settlementDate;

    public Claim() {}

    public Claim(Long id, InsurancePolicy policy, LocalDate incidentDate, LocalDate submissionDate,
                 String claimStatus, String decision, BigDecimal estimatedDamage,
                 String incidentLocation, String description) {
        this.id = id;
        this.policy = policy;
        this.incidentDate = incidentDate != null ? incidentDate : LocalDate.now();
        this.submissionDate = submissionDate != null ? submissionDate : LocalDate.now();
        this.claimStatus = claimStatus != null ? claimStatus : "Submitted";
        this.decision = decision;
        this.estimatedDamage = estimatedDamage;
        this.incidentLocation = incidentLocation != null ? incidentLocation : "Colombo";
        this.description = description;
    }

    public static ClaimBuilder builder() {
        return new ClaimBuilder();
    }

    public static class ClaimBuilder {
        private Long id;
        private InsurancePolicy policy;
        private LocalDate incidentDate = LocalDate.now();
        private LocalDate submissionDate = LocalDate.now();
        private String claimStatus = "Submitted";
        private String decision;
        private BigDecimal estimatedDamage;
        private String incidentLocation = "Colombo";
        private String description;
        private String claimNumber;
        private Double approvedAmount;
        private String documentName = "police_report_verified.pdf";
        private String officerRemarks;
        private LocalDateTime submittedAt = LocalDateTime.now();
        private LocalDate settlementDate;

        public ClaimBuilder id(Long id) { this.id = id; return this; }
        public ClaimBuilder policy(InsurancePolicy policy) { this.policy = policy; return this; }
        public ClaimBuilder incidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; return this; }
        public ClaimBuilder submissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; return this; }
        public ClaimBuilder claimStatus(String claimStatus) { this.claimStatus = claimStatus; return this; }
        public ClaimBuilder status(ClaimStatus status) {
            if (status != null) {
                switch (status) {
                    case SUBMITTED -> this.claimStatus = "Submitted";
                    case UNDER_VERIFICATION, DOCUMENTS_REQUIRED -> this.claimStatus = "Under Review";
                    case APPROVED -> { this.claimStatus = "Approved"; this.decision = "Approved"; }
                    case REJECTED -> { this.claimStatus = "Rejected"; this.decision = "Rejected"; }
                    case SETTLED -> { this.claimStatus = "Settled"; this.decision = "Settled"; }
                }
            }
            return this;
        }
        public ClaimBuilder decision(String decision) { this.decision = decision; return this; }
        public ClaimBuilder estimatedDamage(BigDecimal estimatedDamage) { this.estimatedDamage = estimatedDamage; return this; }
        public ClaimBuilder claimedAmount(Double claimedAmount) {
            this.estimatedDamage = claimedAmount != null ? BigDecimal.valueOf(claimedAmount) : BigDecimal.ZERO;
            return this;
        }
        public ClaimBuilder approvedAmount(Double approvedAmount) { this.approvedAmount = approvedAmount; return this; }
        public ClaimBuilder incidentLocation(String incidentLocation) { this.incidentLocation = incidentLocation; return this; }
        public ClaimBuilder description(String description) { this.description = description; return this; }
        public ClaimBuilder claimNumber(String claimNumber) { this.claimNumber = claimNumber; return this; }
        public ClaimBuilder documentName(String documentName) { this.documentName = documentName; return this; }
        public ClaimBuilder officerRemarks(String officerRemarks) { this.officerRemarks = officerRemarks; return this; }
        public ClaimBuilder submittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; return this; }
        public ClaimBuilder settlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; return this; }

        public Claim build() {
            Claim c = new Claim(id, policy, incidentDate, submissionDate, claimStatus, decision, estimatedDamage, incidentLocation, description);
            c.setClaimNumber(claimNumber);
            c.setApprovedAmount(approvedAmount);
            c.setDocumentName(documentName);
            c.setOfficerRemarks(officerRemarks);
            c.setSubmittedAt(submittedAt);
            c.setSettlementDate(settlementDate);
            return c;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InsurancePolicy getPolicy() { return policy; }
    public void setPolicy(InsurancePolicy policy) { this.policy = policy; }

    public LocalDate getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; }

    public LocalDate getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; }

    public String getClaimStatus() { return claimStatus; }
    public void setClaimStatus(String claimStatus) { this.claimStatus = claimStatus; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public BigDecimal getEstimatedDamage() { return estimatedDamage; }
    public void setEstimatedDamage(BigDecimal estimatedDamage) { this.estimatedDamage = estimatedDamage; }

    public String getIncidentLocation() { return incidentLocation; }
    public void setIncidentLocation(String incidentLocation) { this.incidentLocation = incidentLocation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getClaimNumber() {
        if (claimNumber != null && !claimNumber.isEmpty()) return claimNumber;
        return "CLM-2026-" + String.format("%04d", id != null ? id : (int)(Math.random() * 9000 + 1000));
    }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }

    public Double getClaimedAmount() {
        return estimatedDamage != null ? estimatedDamage.doubleValue() : 0.0;
    }
    public void setClaimedAmount(Double claimedAmount) {
        this.estimatedDamage = claimedAmount != null ? BigDecimal.valueOf(claimedAmount) : BigDecimal.ZERO;
    }

    public Double getApprovedAmount() {
        if (approvedAmount != null) return approvedAmount;
        if ("Approved".equalsIgnoreCase(decision) || "Approved".equalsIgnoreCase(claimStatus)) {
            return getClaimedAmount();
        }
        return 0.0;
    }
    public void setApprovedAmount(Double approvedAmount) { this.approvedAmount = approvedAmount; }

    public ClaimStatus getStatus() {
        if (claimStatus == null) return ClaimStatus.SUBMITTED;
        String s = claimStatus.trim().toUpperCase().replace(" ", "_");
        try {
            return ClaimStatus.valueOf(s);
        } catch (Exception e) {
            if (s.contains("REVIEW") || s.contains("VERIFICATION")) return ClaimStatus.UNDER_VERIFICATION;
            if (s.contains("APPROV")) return ClaimStatus.APPROVED;
            if (s.contains("REJECT")) return ClaimStatus.REJECTED;
            if (s.contains("SETTLE")) return ClaimStatus.SETTLED;
            return ClaimStatus.SUBMITTED;
        }
    }

    public void setStatus(ClaimStatus status) {
        if (status == null) {
            this.claimStatus = "Submitted";
        } else {
            switch (status) {
                case SUBMITTED -> this.claimStatus = "Submitted";
                case UNDER_VERIFICATION, DOCUMENTS_REQUIRED -> this.claimStatus = "Under Review";
                case APPROVED -> { this.claimStatus = "Approved"; this.decision = "Approved"; }
                case REJECTED -> { this.claimStatus = "Rejected"; this.decision = "Rejected"; }
                case SETTLED -> { this.claimStatus = "Settled"; this.decision = "Settled"; }
            }
        }
    }

    public String getDocumentName() { return documentName != null ? documentName : "police_report_verified.pdf"; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getOfficerRemarks() { return officerRemarks != null ? officerRemarks : decision; }
    public void setOfficerRemarks(String officerRemarks) { this.officerRemarks = officerRemarks; }

    public LocalDateTime getSubmittedAt() {
        if (submittedAt != null) return submittedAt;
        return submissionDate != null ? submissionDate.atStartOfDay() : LocalDateTime.now();
    }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDate getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; }
}
