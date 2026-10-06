package com.insuradrive.service.factory.report;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportFactoryProducer {

    private final List<ReportFactory> factories;
    private final ComplianceAuditReportFactory defaultFactory;

    public ReportFactoryProducer(List<ReportFactory> factories,
                                 ComplianceAuditReportFactory defaultFactory) {
        this.factories = factories;
        this.defaultFactory = defaultFactory;
    }

    public ReportFactory getFactory(String reportType) {
        if (reportType == null || reportType.trim().isEmpty()) {
            return defaultFactory;
        }
        return factories.stream()
                .filter(f -> f.getReportType().equalsIgnoreCase(reportType.trim()))
                .findFirst()
                .orElse(defaultFactory);
    }
}
