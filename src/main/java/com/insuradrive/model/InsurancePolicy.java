package com.insuradrive.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "POLICY")
public class InsurancePolicy {

    public enum PolicyType {
        COMPREHENSIVE_PREMIUM,
        COMPREHENSIVE_STANDARD,
        THIRD_PARTY_ONLY,
        THIRD_PARTY_FIRE_THEFT
    }

    public enum PolicyStatus {
        ACTIVE,
        PENDING_PAYMENT,
        PENDING_VERIFICATION,
        SUSPENDED,
        EXPIRED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policyID")
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "applicationID", nullable = false, unique = true)
    private PolicyApplication application;

    @Column(name = "policyNumber", length = 50, nullable = false, unique = true)
    private String policyNumber;

    @Column(name = "issueDate", nullable = false)
    private LocalDate issueDate = LocalDate.now();

    @Column(name = "startDate", nullable = false)
    private LocalDate startDate = LocalDate.now();

    @Column(name = "expiryDate", nullable = false)
    private LocalDate expiryDate = LocalDate.now().plusYears(1);

    @Column(name = "premiumAmount", precision = 12, scale = 2, nullable = false)
    private BigDecimal premiumAmount = BigDecimal.ZERO;

    @Column(name = "policyStatus", length = 30, nullable = false)
    private String policyStatus = "Active";

    @Transient
    private PolicyType policyType;

    @Transient
    private Double sumInsured = 5000000.0;

    @Transient
    private Integer renewalCount = 0;

    @Transient
    private String cancellationReason;

    public InsurancePolicy() {}

    public InsurancePolicy(Long id, PolicyApplication application, String policyNumber,
                           LocalDate issueDate, LocalDate startDate, LocalDate expiryDate,
                           BigDecimal premiumAmount, String policyStatus) {
        this.id = id;
        this.application = application;
        this.policyNumber = policyNumber;
        this.issueDate = issueDate != null ? issueDate : LocalDate.now();
        this.startDate = startDate != null ? startDate : LocalDate.now();
        this.expiryDate = expiryDate != null ? expiryDate : LocalDate.now().plusYears(1);
        this.premiumAmount = premiumAmount != null ? premiumAmount : BigDecimal.ZERO;
        this.policyStatus = policyStatus != null ? policyStatus : "Active";
    }

    public static InsurancePolicyBuilder builder() {
        return new InsurancePolicyBuilder();
    }

    public static class InsurancePolicyBuilder {
        private Long id;
        private PolicyApplication application;
        private String policyNumber;
        private LocalDate issueDate = LocalDate.now();
        private LocalDate startDate = LocalDate.now();
        private LocalDate expiryDate = LocalDate.now().plusYears(1);
        private BigDecimal premiumAmount = BigDecimal.ZERO;
        private String policyStatus = "Active";
        private PolicyType policyType;
        private Double sumInsured = 5000000.0;
        private Integer renewalCount = 0;
        private String cancellationReason;
        private Customer customer;
        private Vehicle vehicle;

        public InsurancePolicyBuilder id(Long id) { this.id = id; return this; }
        public InsurancePolicyBuilder application(PolicyApplication application) { this.application = application; return this; }
        public InsurancePolicyBuilder policyNumber(String policyNumber) { this.policyNumber = policyNumber; return this; }
        public InsurancePolicyBuilder issueDate(LocalDate issueDate) { this.issueDate = issueDate; return this; }
        public InsurancePolicyBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public InsurancePolicyBuilder expiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; return this; }
        public InsurancePolicyBuilder endDate(LocalDate endDate) { this.expiryDate = endDate; return this; }
        public InsurancePolicyBuilder premiumAmount(BigDecimal premiumAmount) { this.premiumAmount = premiumAmount; return this; }
        public InsurancePolicyBuilder annualPremium(Double annualPremium) {
            this.premiumAmount = annualPremium != null ? BigDecimal.valueOf(annualPremium) : BigDecimal.ZERO;
            return this;
        }
        public InsurancePolicyBuilder policyStatus(String policyStatus) { this.policyStatus = policyStatus; return this; }
        public InsurancePolicyBuilder status(PolicyStatus status) {
            this.policyStatus = status != null ? status.name() : "Active";
            return this;
        }
        public InsurancePolicyBuilder policyType(PolicyType policyType) { this.policyType = policyType; return this; }
        public InsurancePolicyBuilder sumInsured(Double sumInsured) { this.sumInsured = sumInsured; return this; }
        public InsurancePolicyBuilder renewalCount(Integer renewalCount) { this.renewalCount = renewalCount; return this; }
        public InsurancePolicyBuilder cancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; return this; }
        public InsurancePolicyBuilder customer(Customer customer) { this.customer = customer; return this; }
        public InsurancePolicyBuilder vehicle(Vehicle vehicle) { this.vehicle = vehicle; return this; }

        public InsurancePolicy build() {
            if (this.application == null && (this.customer != null || this.vehicle != null)) {
                this.application = new PolicyApplication();
                this.application.setCustomer(this.customer);
                this.application.setVehicle(this.vehicle);
                this.application.setCalculatedPremium(this.premiumAmount);
                this.application.setStatus("Approved");
                this.application.setDecision("Approved");
            }
            InsurancePolicy p = new InsurancePolicy(id, application, policyNumber, issueDate, startDate, expiryDate, premiumAmount, policyStatus);
            p.setPolicyType(policyType);
            p.setSumInsured(sumInsured);
            p.setRenewalCount(renewalCount != null ? renewalCount : 0);
            p.setCancellationReason(cancellationReason);
            return p;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PolicyApplication getApplication() { return application; }
    public void setApplication(PolicyApplication application) { this.application = application; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public LocalDate getEndDate() { return expiryDate; }
    public void setEndDate(LocalDate endDate) { this.expiryDate = endDate; }

    public BigDecimal getPremiumAmount() { return premiumAmount; }
    public void setPremiumAmount(BigDecimal premiumAmount) { this.premiumAmount = premiumAmount; }

    public Double getAnnualPremium() {
        return premiumAmount != null ? premiumAmount.doubleValue() : 0.0;
    }
    public void setAnnualPremium(Double annualPremium) {
        this.premiumAmount = annualPremium != null ? BigDecimal.valueOf(annualPremium) : BigDecimal.ZERO;
    }

    public String getPolicyStatus() { return policyStatus; }
    public void setPolicyStatus(String policyStatus) { this.policyStatus = policyStatus; }

    public PolicyStatus getStatus() {
        if (policyStatus == null) return PolicyStatus.ACTIVE;
        String s = policyStatus.trim().toUpperCase().replace(" ", "_");
        try {
            return PolicyStatus.valueOf(s);
        } catch (Exception e) {
            if (s.contains("EXPIRED")) return PolicyStatus.EXPIRED;
            if (s.contains("CANCEL")) return PolicyStatus.CANCELLED;
            if (s.contains("PENDING")) return PolicyStatus.PENDING_PAYMENT;
            if (s.contains("SUSPEND")) return PolicyStatus.SUSPENDED;
            return PolicyStatus.ACTIVE;
        }
    }

    public void setStatus(PolicyStatus status) {
        if (status == null) {
            this.policyStatus = "Active";
        } else {
            switch (status) {
                case ACTIVE -> this.policyStatus = "Active";
                case PENDING_PAYMENT -> this.policyStatus = "Pending Payment";
                case PENDING_VERIFICATION -> this.policyStatus = "Pending Review";
                case SUSPENDED -> this.policyStatus = "Suspended";
                case EXPIRED -> this.policyStatus = "Expired";
                case CANCELLED -> this.policyStatus = "Cancelled";
            }
        }
    }

    public Customer getCustomer() {
        return application != null ? application.getCustomer() : null;
    }

    public void setCustomer(Customer customer) {
        if (this.application == null) {
            this.application = new PolicyApplication();
        }
        this.application.setCustomer(customer);
    }

    public Vehicle getVehicle() {
        return application != null ? application.getVehicle() : null;
    }

    public void setVehicle(Vehicle vehicle) {
        if (this.application == null) {
            this.application = new PolicyApplication();
        }
        this.application.setVehicle(vehicle);
    }

    public Double getSumInsured() {
        if (sumInsured != null) return sumInsured;
        if (getVehicle() != null && getVehicle().getEstimatedValue() != null) {
            return getVehicle().getEstimatedValue();
        }
        return 5000000.0;
    }

    public void setSumInsured(Double sumInsured) {
        this.sumInsured = sumInsured;
    }

    public PolicyType getPolicyType() {
        if (this.policyType != null) return this.policyType;
        if (application != null && application.getInsurancePackage() != null) {
            String name = application.getInsurancePackage().getPackageName().toUpperCase();
            if (name.contains("PREMIUM")) return PolicyType.COMPREHENSIVE_PREMIUM;
            if (name.contains("FIRE") || name.contains("THEFT")) return PolicyType.THIRD_PARTY_FIRE_THEFT;
            if (name.contains("THIRD")) return PolicyType.THIRD_PARTY_ONLY;
            return PolicyType.COMPREHENSIVE_STANDARD;
        }
        return PolicyType.COMPREHENSIVE_STANDARD;
    }

    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }

    public Integer getRenewalCount() { return renewalCount != null ? renewalCount : 0; }
    public void setRenewalCount(Integer renewalCount) { this.renewalCount = renewalCount; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
}
