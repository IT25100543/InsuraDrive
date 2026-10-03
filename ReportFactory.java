package com.insuradrive.service.factory.report;

import com.insuradrive.model.Report;
import com.insuradrive.model.Staff;
import java.time.LocalDate;
import java.util.Map;

/**
 * Factory Method Pattern: Abstract Creator for Report Generation.
 * Encapsulates report record instantiation and domain metrics compilation.
 */
public interface ReportFactory {
    String getReportType();
    Report createReport(Staff staff, String branch, LocalDate startDate, LocalDate endDate, String format);
    Map<String, Object> compileMetrics(String branch, LocalDate startDate, LocalDate endDate);
}
