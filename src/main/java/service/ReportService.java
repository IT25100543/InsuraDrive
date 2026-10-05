package com.insuradrive.service;

import com.insuradrive.model.Claim;
import com.insuradrive.model.InsurancePolicy;
import com.insuradrive.model.Report;
import com.insuradrive.model.Staff;
import com.insuradrive.repository.*;
import com.insuradrive.service.factory.report.ReportFactory;
import com.insuradrive.service.factory.report.ReportFactoryProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for UC-27: Generate Compliance & Audit Report.
 * Uses Report Factory Pattern to generate specialized regulatory reports
 * and manages complete CRUD persistence in SQL Server table REPORT.
 */
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final StaffRepository staffRepository;
    private final ReportFactoryProducer factoryProducer;
    private final InsurancePolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;

    public ReportService(ReportRepository reportRepository,
                         StaffRepository staffRepository,
                         ReportFactoryProducer factoryProducer,
                         InsurancePolicyRepository policyRepository,
                         ClaimRepository claimRepository,
                         AuditLogRepository auditLogRepository,
                         AuditService auditService) {
        this.reportRepository = reportRepository;
        this.staffRepository = staffRepository;
        this.factoryProducer = factoryProducer;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
    }

    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByGeneratedDateDesc();
    }

    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    @Transactional
    public Report createAndSaveReport(Staff staff, String branch, String reportType,
                                      LocalDate startDate, LocalDate endDate, String format) {
        if (staff == null) {
            staff = staffRepository.findAll().stream().findFirst().orElse(null);
        }

        ReportFactory factory = factoryProducer.getFactory(reportType);
        Report report = factory.createReport(staff, branch, startDate, endDate, format);
        return reportRepository.save(report);
    }

    @Transactional
    public Report updateReport(Long id, String format, LocalDate startDate, LocalDate endDate, String notes) {
        Report report = reportRepository.findById(id).orElse(null);
        if (report != null) {
            if (format != null && !format.trim().isEmpty()) {
                report.setFormat(format.trim());
            }
            if (startDate != null) {
                report.setDateFrom(startDate);
            }
            if (endDate != null) {
                report.setDateTo(endDate);
            }
            if (notes != null) {
                report.setNotes(notes.trim());
            }
            return reportRepository.save(report);
        }
        return null;
    }

    @Transactional
    public boolean deleteReport(Long id) {
        if (reportRepository.existsById(id)) {
            reportRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Map<String, Object> compileComplianceReport(String branch, String reportType,
                                                       LocalDate startDate, LocalDate endDate) {
        ReportFactory factory = factoryProducer.getFactory(reportType);
        Map<String, Object> data = factory.compileMetrics(branch, startDate, endDate);

        data.putIfAbsent("branch", branch != null ? branch : "Colombo Central Main Branch");
        data.putIfAbsent("reportType", reportType != null ? reportType : "Comprehensive Regulatory Audit");
        data.putIfAbsent("reportTitle", reportType != null ? reportType : "Comprehensive Regulatory Audit");
        data.putIfAbsent("complianceStatus", "HEALTHY");
        data.putIfAbsent("generatedDate", LocalDate.now().toString());

        long totalPolicies = policyRepository.count();
        long activePolicies = policyRepository.findByStatus(InsurancePolicy.PolicyStatus.ACTIVE).size();
        double totalPremiums = policyRepository.findAll().stream().mapToDouble(InsurancePolicy::getAnnualPremium).sum();

        List<Claim> allClaims = claimRepository.findAll();
        long approvedClaims = allClaims.stream().filter(c -> c.getStatus() == Claim.ClaimStatus.APPROVED || c.getStatus() == Claim.ClaimStatus.SETTLED).count();
        double totalApprovedClaims = allClaims.stream()
                .filter(c -> c.getStatus() == Claim.ClaimStatus.APPROVED || c.getStatus() == Claim.ClaimStatus.SETTLED)
                .mapToDouble(Claim::getApprovedAmount).sum();

        double lossRatio = totalPremiums > 0 ? (totalApprovedClaims / totalPremiums) * 100 : 0.0;

        data.putIfAbsent("totalPolicies", totalPolicies);
        data.putIfAbsent("activePolicies", activePolicies);
        data.putIfAbsent("totalPremiums", totalPremiums);
        data.putIfAbsent("totalApprovedClaims", totalApprovedClaims);
        data.putIfAbsent("approvedClaims", approvedClaims);
        data.putIfAbsent("lossRatio", String.format("%.1f", lossRatio));
        data.putIfAbsent("auditLogs", auditLogRepository.findAllByOrderByTimestampDesc().stream().limit(10).toList());

        return data;
    }
}
