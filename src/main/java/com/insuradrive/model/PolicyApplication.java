package com.insuradrive.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "POLICY_APPLICATION")
public class PolicyApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "applicationID")
    private Integer applicationID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customerID", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicleID", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "packageID", nullable = false)
    private InsurancePackage insurancePackage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsibleStaffID")
    private Staff responsibleStaff;

    @Column(name = "calculatedPremium", precision = 12, scale = 2, nullable = false)
    private BigDecimal calculatedPremium;

    @Column(name = "applicationDate", nullable = false)
    private LocalDate applicationDate = LocalDate.now();

    @Column(name = "status", length = 30, nullable = false)
    private String status = "Pending Review";

    @Column(name = "decision", length = 30)
    private String decision;

    @Column(name = "decisionDate")
    private LocalDate decisionDate;

    @Column(name = "decisionComments", length = 500)
    private String decisionComments;

    @Transient
    private InsurancePolicy policy;

    public PolicyApplication() {}

    public PolicyApplication(Integer applicationID, Customer customer, Vehicle vehicle,
                             InsurancePackage insurancePackage, Staff responsibleStaff,
                             BigDecimal calculatedPremium, LocalDate applicationDate,
                             String status, String decision, LocalDate decisionDate, String decisionComments) {
        this.applicationID = applicationID;
        this.customer = customer;
        this.vehicle = vehicle;
        this.insurancePackage = insurancePackage;
        this.responsibleStaff = responsibleStaff;
        this.calculatedPremium = calculatedPremium;
        this.applicationDate = applicationDate != null ? applicationDate : LocalDate.now();
        this.status = status != null ? status : "Pending Review";
        this.decision = decision;
        this.decisionDate = decisionDate;
        this.decisionComments = decisionComments;
    }

    public static PolicyApplicationBuilder builder() {
        return new PolicyApplicationBuilder();
    }

    public static class PolicyApplicationBuilder {
        private Integer applicationID;
        private Customer customer;
        private Vehicle vehicle;
        private InsurancePackage insurancePackage;
        private Staff responsibleStaff;
        private BigDecimal calculatedPremium;
        private LocalDate applicationDate = LocalDate.now();
        private String status = "Pending Review";
        private String decision;
        private LocalDate decisionDate;
        private String decisionComments;

        public PolicyApplicationBuilder applicationID(Integer applicationID) { this.applicationID = applicationID; return this; }
        public PolicyApplicationBuilder customer(Customer customer) { this.customer = customer; return this; }
        public PolicyApplicationBuilder vehicle(Vehicle vehicle) { this.vehicle = vehicle; return this; }
        public PolicyApplicationBuilder insurancePackage(InsurancePackage insurancePackage) { this.insurancePackage = insurancePackage; return this; }
        public PolicyApplicationBuilder responsibleStaff(Staff responsibleStaff) { this.responsibleStaff = responsibleStaff; return this; }
        public PolicyApplicationBuilder calculatedPremium(BigDecimal calculatedPremium) { this.calculatedPremium = calculatedPremium; return this; }
        public PolicyApplicationBuilder applicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; return this; }
        public PolicyApplicationBuilder status(String status) { this.status = status; return this; }
        public PolicyApplicationBuilder decision(String decision) { this.decision = decision; return this; }
        public PolicyApplicationBuilder decisionDate(LocalDate decisionDate) { this.decisionDate = decisionDate; return this; }
        public PolicyApplicationBuilder decisionComments(String decisionComments) { this.decisionComments = decisionComments; return this; }

        public PolicyApplication build() {
            return new PolicyApplication(applicationID, customer, vehicle, insurancePackage, responsibleStaff, calculatedPremium, applicationDate, status, decision, decisionDate, decisionComments);
        }
    }

    public Integer getApplicationID() { return applicationID; }
    public void setApplicationID(Integer applicationID) { this.applicationID = applicationID; }

    public Integer getId() { return applicationID; }
    public void setId(Integer id) { this.applicationID = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public InsurancePackage getInsurancePackage() { return insurancePackage; }
    public void setInsurancePackage(InsurancePackage insurancePackage) { this.insurancePackage = insurancePackage; }

    public Staff getResponsibleStaff() { return responsibleStaff; }
    public void setResponsibleStaff(Staff responsibleStaff) { this.responsibleStaff = responsibleStaff; }

    public BigDecimal getCalculatedPremium() { return calculatedPremium; }
    public void setCalculatedPremium(BigDecimal calculatedPremium) { this.calculatedPremium = calculatedPremium; }

    public LocalDate getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public LocalDate getDecisionDate() { return decisionDate; }
    public void setDecisionDate(LocalDate decisionDate) { this.decisionDate = decisionDate; }

    public String getDecisionComments() { return decisionComments; }
    public void setDecisionComments(String decisionComments) { this.decisionComments = decisionComments; }

    public InsurancePolicy getPolicy() { return policy; }
    public void setPolicy(InsurancePolicy policy) { this.policy = policy; }

    public boolean isApproved() {
        return "Approved".equalsIgnoreCase(status);
    }
}
