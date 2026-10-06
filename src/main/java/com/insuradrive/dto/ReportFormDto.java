package com.insuradrive.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Maps to table REPORT in InsuraDrive_DDD.
 * Linked to STAFF(userID).
 * Represents UC-27 (Generate Compliance & Audit Report).
 */
@Entity
@Table(name = "REPORT")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reportID")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "staffID", nullable = false)
    private Staff staff;

    @Column(name = "generatedDate", nullable = false)
    private LocalDateTime generatedDate = LocalDateTime.now();

    @Column(name = "dateFrom")
    private LocalDate dateFrom;

    @Column(name = "dateTo")
    private LocalDate dateTo;

    @Column(name = "format", length = 20, nullable = false)
    private String format = "PDF";

    @Transient
    private String reportType = "Comprehensive Regulatory Audit";

    @Transient
    private String branch = "Colombo Central Main Branch";

    @Transient
    private String status = "Archived";

    @Transient
    private String notes;

    public Report() {}

    public Report(Long id, Staff staff, LocalDateTime generatedDate, LocalDate dateFrom, LocalDate dateTo, String format) {
        this.id = id;
        this.staff = staff;
        this.generatedDate = generatedDate != null ? generatedDate : LocalDateTime.now();
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.format = format != null ? format : "PDF";
    }

    public static ReportBuilder builder() {
        return new ReportBuilder();
    }

    public static class ReportBuilder {
        private Long id;
        private Staff staff;
        private LocalDateTime generatedDate = LocalDateTime.now();
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private String format = "PDF";
        private String reportType = "Comprehensive Regulatory Audit";
        private String branch = "Colombo Central Main Branch";
        private String status = "Archived";
        private String notes;

        public ReportBuilder id(Long id) { this.id = id; return this; }
        public ReportBuilder staff(Staff staff) { this.staff = staff; return this; }
        public ReportBuilder generatedDate(LocalDateTime generatedDate) { this.generatedDate = generatedDate; return this; }
        public ReportBuilder dateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; return this; }
        public ReportBuilder dateTo(LocalDate dateTo) { this.dateTo = dateTo; return this; }
        public ReportBuilder format(String format) { this.format = format; return this; }
        public ReportBuilder reportType(String reportType) { this.reportType = reportType; return this; }
        public ReportBuilder branch(String branch) { this.branch = branch; return this; }
        public ReportBuilder status(String status) { this.status = status; return this; }
        public ReportBuilder notes(String notes) { this.notes = notes; return this; }

        public Report build() {
            Report r = new Report(id, staff, generatedDate, dateFrom, dateTo, format);
            r.setReportType(reportType);
            r.setBranch(branch);
            r.setStatus(status);
            r.setNotes(notes);
            return r;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Staff getStaff() { return staff; }
    public void setStaff(Staff staff) { this.staff = staff; }

    public LocalDateTime getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDateTime generatedDate) { this.generatedDate = generatedDate; }

    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }

    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getReferenceCode() {
        return "RPT-2026-" + String.format("%04d", id != null ? id : 1);
    }
}
