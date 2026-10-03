package com.insuradrive.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "INSURANCE_PACKAGE")
public class InsurancePackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "packageID")
    private Integer packageID;

    @Column(name = "packageName", length = 100, nullable = false)
    private String packageName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "coverageType", length = 100, nullable = false)
    private String coverageType;

    @Column(name = "basePremium", precision = 12, scale = 2, nullable = false)
    private BigDecimal basePremium;

    @OneToMany(mappedBy = "insurancePackage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PolicyApplication> applications = new ArrayList<>();

    public InsurancePackage() {}

    public InsurancePackage(Integer packageID, String packageName, String description,
                            String coverageType, BigDecimal basePremium) {
        this.packageID = packageID;
        this.packageName = packageName;
        this.description = description;
        this.coverageType = coverageType;
        this.basePremium = basePremium;
    }

    public static InsurancePackageBuilder builder() {
        return new InsurancePackageBuilder();
    }

    public static class InsurancePackageBuilder {
        private Integer packageID;
        private String packageName;
        private String description;
        private String coverageType;
        private BigDecimal basePremium;

        public InsurancePackageBuilder packageID(Integer packageID) { this.packageID = packageID; return this; }
        public InsurancePackageBuilder packageName(String packageName) { this.packageName = packageName; return this; }
        public InsurancePackageBuilder description(String description) { this.description = description; return this; }
        public InsurancePackageBuilder coverageType(String coverageType) { this.coverageType = coverageType; return this; }
        public InsurancePackageBuilder basePremium(BigDecimal basePremium) { this.basePremium = basePremium; return this; }

        public InsurancePackage build() {
            return new InsurancePackage(packageID, packageName, description, coverageType, basePremium);
        }
    }

    public Integer getPackageID() { return packageID; }
    public void setPackageID(Integer packageID) { this.packageID = packageID; }

    public Integer getId() { return packageID; }
    public void setId(Integer id) { this.packageID = id; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverageType() { return coverageType; }
    public void setCoverageType(String coverageType) { this.coverageType = coverageType; }

    public BigDecimal getBasePremium() { return basePremium; }
    public void setBasePremium(BigDecimal basePremium) { this.basePremium = basePremium; }

    public List<PolicyApplication> getApplications() { return applications; }
    public void setApplications(List<PolicyApplication> applications) { this.applications = applications; }
}
