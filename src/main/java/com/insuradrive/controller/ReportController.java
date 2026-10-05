package com.insuradrive.controller;

import com.insuradrive.dto.ReportFormDto;
import com.insuradrive.model.Report;
import com.insuradrive.model.Staff;
import com.insuradrive.repository.StaffRepository;
import com.insuradrive.service.AuditService;
import com.insuradrive.service.ReportService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for Administration, Reporting & Compliance Module (UC-27: Generate Compliance & Audit Report).
 * Demonstrates:
 * 1. Complete CRUD (Create/Insert, Read/View, Update, Delete)
 * 2. Factory Design Pattern via ReportFactoryProducer & concrete Report factories
 * 3. Jakarta Bean Validation via ReportFormDto
 * 4. SQL Server persistence to table REPORT
 */
@Controller
public class ReportController {

    private final ReportService reportService;
    private final StaffRepository staffRepository;
    private final AuditService auditService;

    public ReportController(ReportService reportService,
                            StaffRepository staffRepository,
                            AuditService auditService) {
        this.reportService = reportService;
        this.staffRepository = staffRepository;
        this.auditService = auditService;
    }

    private void populateBranchesAndTypes(Model model) {
        model.addAttribute("branches", new String[]{
                "Colombo Central Main Branch",
                "Kandy Regional Branch",
                "Galle Branch",
                "Kurunegala Branch",
                "Gampaha Branch"
        });
        model.addAttribute("reportTypes", new String[]{
                "Comprehensive Regulatory Audit",
                "Claims Loss & Risk Assessment",
                "Policy Underwriting Compliance",
                "Overdue Premium Exposure"
        });
    }

    @GetMapping("/reports")
    public String listReports(Model model, HttpSession session) {
        List<Report> reports = reportService.getAllReports();
        model.addAttribute("reports", reports);
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "reports/list";
    }

    @GetMapping({"/reports/compliance", "/reports/new"})
    public String showReportForm(Model model, HttpSession session) {
        model.addAttribute("reportFormDto", new ReportFormDto());
        populateBranchesAndTypes(model);
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "reports/form";
    }

    @PostMapping("/reports/generate")
    public String generateReport(@Valid @ModelAttribute("reportFormDto") ReportFormDto formDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateBranchesAndTypes(model);
            model.addAttribute("activeRole", session.getAttribute("activeRole"));
            return "reports/form";
        }

        // Get currently logged in staff or default to first staff
        Staff staff = staffRepository.findAll().stream().findFirst().orElse(null);

        // Factory Pattern execution: ReportFactoryProducer delegates to concrete factory
        // and persists to SQL Server table REPORT
        Report savedReport = reportService.createAndSaveReport(
                staff,
                formDto.getBranch(),
                formDto.getReportType(),
                formDto.getStartDate(),
                formDto.getEndDate(),
                formDto.getFormat()
        );

        // Compile metrics for the view using Factory
        Map<String, Object> reportData = reportService.compileComplianceReport(
                formDto.getBranch(),
                formDto.getReportType(),
                formDto.getStartDate(),
                formDto.getEndDate()
        );

        model.addAttribute("report", reportData);
        model.addAttribute("savedReport", savedReport);
        model.addAttribute("startDate", formDto.getStartDate());
        model.addAttribute("endDate", formDto.getEndDate());
        model.addAttribute("activeRole", session.getAttribute("activeRole"));

        String activeRole = (String) session.getAttribute("activeRole");
        auditService.logAction("risk_officer", activeRole, "GENERATE_COMPLIANCE_REPORT", "Administration & Compliance",
                "Generated and saved " + formDto.getReportType() + " (ID: " + savedReport.getId() + ") for " + formDto.getBranch() + " (Period: " + formDto.getStartDate() + " to " + formDto.getEndDate() + ")");

        return "reports/view";
    }

    @GetMapping("/reports/view/{id}")
    public String viewReport(@PathVariable("id") Long id,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Report report = reportService.getReportById(id).orElse(null);
        if (report == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Report not found");
            return "redirect:/reports";
        }

        Map<String, Object> reportData = reportService.compileComplianceReport(
                report.getBranch(),
                report.getReportType(),
                report.getDateFrom(),
                report.getDateTo()
        );

        model.addAttribute("report", reportData);
        model.addAttribute("savedReport", report);
        model.addAttribute("startDate", report.getDateFrom());
        model.addAttribute("endDate", report.getDateTo());
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "reports/view";
    }

    @GetMapping("/reports/edit/{id}")
    public String editReportForm(@PathVariable("id") Long id,
                                 Model model,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Report report = reportService.getReportById(id).orElse(null);
        if (report == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Report not found");
            return "redirect:/reports";
        }

        ReportFormDto formDto = new ReportFormDto();
        formDto.setId(report.getId());
        formDto.setBranch(report.getBranch());
        formDto.setReportType(report.getReportType());
        formDto.setStartDate(report.getDateFrom());
        formDto.setEndDate(report.getDateTo());
        formDto.setFormat(report.getFormat());
        formDto.setNotes(report.getNotes());

        model.addAttribute("reportFormDto", formDto);
        model.addAttribute("report", report);
        populateBranchesAndTypes(model);
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "reports/edit";
    }

    @PostMapping("/reports/update/{id}")
    public String updateReport(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("reportFormDto") ReportFormDto formDto,
                               BindingResult bindingResult,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Report report = reportService.getReportById(id).orElse(null);
        if (report == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Report not found");
            return "redirect:/reports";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("report", report);
            populateBranchesAndTypes(model);
            model.addAttribute("activeRole", session.getAttribute("activeRole"));
            return "reports/edit";
        }

        reportService.updateReport(id, formDto.getFormat(), formDto.getStartDate(), formDto.getEndDate(), formDto.getNotes());

        String activeRole = (String) session.getAttribute("activeRole");
        auditService.logAction("risk_officer", activeRole, "UPDATE_REPORT", "Administration & Compliance",
                "Updated report ID: " + id + " metadata.");

        redirectAttributes.addFlashAttribute("successMessage", "Report ID " + id + " updated successfully.");
        return "redirect:/reports";
    }

    @PostMapping("/reports/delete/{id}")
    public String deleteReport(@PathVariable("id") Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        boolean deleted = reportService.deleteReport(id);
        if (deleted) {
            String activeRole = (String) session.getAttribute("activeRole");
            auditService.logAction("compliance", activeRole, "DELETE_REPORT", "Administration & Compliance",
                    "Deleted compliance report ID: " + id);
            redirectAttributes.addFlashAttribute("successMessage", "Report ID " + id + " successfully deleted from SQL Server.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Report not found or could not be deleted.");
        }
        return "redirect:/reports";
    }

    @PostMapping("/reports/share")
    public String shareWithManagement(@RequestParam("branch") String branch,
                                      @RequestParam("reportType") String reportType,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        String activeRole = (String) session.getAttribute("activeRole");
        auditService.logAction("risk_officer", activeRole, "SHARE_REPORT_MANAGEMENT", "Administration & Compliance",
                "Shared compliance report '" + reportType + "' directly with Branch Operations Manager for review.");

        redirectAttributes.addFlashAttribute("successMessage", "Compliance report successfully dispatched to Branch Management.");
        return "redirect:/reports";
    }
}
