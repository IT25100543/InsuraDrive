package com.insuradrive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ReportFormDto {

    private Long id;

    @NotBlank(message = "Branch selection is required")
    private String branch = "Colombo Central Main Branch";

    @NotBlank(message = "Report type is required")
    private String reportType = "Comprehensive Regulatory Audit";

    @NotNull(message = "Period start date is required")
    private LocalDate startDate = LocalDate.now().minusMonths(6);

    @NotNull(message = "Period end date is required")
    private LocalDate endDate = LocalDate.now();

    @NotBlank(message = "Export format is required (PDF or Excel)")
    private String format = "PDF";

    private String notes;

    public ReportFormDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
