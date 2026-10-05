package com.insuradrive.controller;

import com.insuradrive.dto.PolicyFormDto;
import com.insuradrive.model.*;
import com.insuradrive.repository.*;
import com.insuradrive.service.AuditService;
import com.insuradrive.service.InsuranceCalculationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for Insurance Policy Management Module (UC-11: Apply / Issue Insurance Policy).
 * Demonstrates:
 * - Full CRUD (Issue Policy, View Policies, Renew / Update, Cancel / Void)
 * - Jakarta Bean Validation & Business Rules (@Valid, BindingResult)
 * - Strategy Design Pattern (PremiumCalculationStrategy / InsuranceCalculationService)
 * - Relational persistence in SQL Server tables POLICY_APPLICATION and POLICY.
 */
@Controller
public class PolicyController {

    private final InsurancePolicyRepository policyRepository;
    private final PolicyApplicationRepository applicationRepository;
    private final InsurancePackageRepository packageRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final InsuranceCalculationService calculationService;
    private final AuditService auditService;

    public PolicyController(InsurancePolicyRepository policyRepository,
                            PolicyApplicationRepository applicationRepository,
                            InsurancePackageRepository packageRepository,
                            CustomerRepository customerRepository,
                            VehicleRepository vehicleRepository,
                            InsuranceCalculationService calculationService,
                            AuditService auditService) {
        this.policyRepository = policyRepository;
        this.applicationRepository = applicationRepository;
        this.packageRepository = packageRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.calculationService = calculationService;
        this.auditService = auditService;
    }

    @GetMapping("/policies")
    public String listPolicies(@RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "search", required = false) String search,
                               Model model, HttpSession session) {
        List<InsurancePolicy> policies;
        if (status != null && !status.isEmpty()) {
            policies = policyRepository.findByPolicyStatusContainingIgnoreCase(status);
        } else if (search != null && !search.trim().isEmpty()) {
            policies = policyRepository.findAll().stream()
                    .filter(p -> (p.getPolicyNumber() != null && p.getPolicyNumber().toLowerCase().contains(search.trim().toLowerCase()))
                            || (p.getCustomer() != null && p.getCustomer().getFullName().toLowerCase().contains(search.trim().toLowerCase()))
                            || (p.getVehicle() != null && p.getVehicle().getRegistrationNo().toLowerCase().contains(search.trim().toLowerCase())))
                    .toList();
        } else {
            policies = policyRepository.findAll();
        }
        model.addAttribute("policies", policies);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchQuery", search);
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "policies/list";
    }

    @GetMapping("/policies/new")
    public String newPolicyForm(Model model, HttpSession session) {
        if (!model.containsAttribute("policy")) {
            model.addAttribute("policy", new PolicyFormDto());
        }
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("vehicles", vehicleRepository.findAll());
        model.addAttribute("policyTypes", InsurancePolicy.PolicyType.values());
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "policies/form";
    }

    @PostMapping("/policies/save")
    public String savePolicy(@Valid @ModelAttribute("policy") PolicyFormDto formDto,
                             BindingResult bindingResult,
                             Model model, HttpSession session,
                             RedirectAttributes redirectAttributes) {
        // Business Validation: Expiry date must be after Start date
        if (formDto.getStartDate() != null && formDto.getExpiryDate() != null) {
            if (!formDto.getExpiryDate().isAfter(formDto.getStartDate())) {
                bindingResult.rejectValue("expiryDate", "error.expiry", "Expiry date must be after policy start date.");
            }
        }

        Vehicle vehicle = formDto.getVehicleId() != null ? vehicleRepository.findById(formDto.getVehicleId()).orElse(null) : null;
        Customer customer = formDto.getCustomerId() != null ? customerRepository.findById(formDto.getCustomerId()).orElse(null) : null;

        if (vehicle == null) {
            bindingResult.rejectValue("vehicleId", "error.vehicle", "Selected vehicle not found.");
        } else if (vehicle.getInspectionStatus() == Vehicle.InspectionStatus.FAILED) {
            bindingResult.rejectValue("vehicleId", "error.vehicle", "Vehicle has failed roadworthiness inspection. Cannot issue policy.");
        }

        if (customer == null) {
            bindingResult.rejectValue("customerId", "error.customer", "Selected customer not found.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", customerRepository.findAll());
            model.addAttribute("vehicles", vehicleRepository.findAll());
            model.addAttribute("policyTypes", InsurancePolicy.PolicyType.values());
            model.addAttribute("activeRole", session.getAttribute("activeRole"));
            return "policies/form";
        }

        InsurancePolicy.PolicyType policyType;
        try {
            policyType = InsurancePolicy.PolicyType.valueOf(formDto.getPolicyType());
        } catch (Exception e) {
            policyType = InsurancePolicy.PolicyType.COMPREHENSIVE_STANDARD;
        }

        Double estimatedValue = vehicle.getEstimatedValue() != null ? vehicle.getEstimatedValue() : 5000000.0;
        Integer cc = vehicle.getEngineCapacity() != null ? vehicle.getEngineCapacity() : 1500;
        Integer year = vehicle.getManufacturerYear() != null ? vehicle.getManufacturerYear() : 2020;

        // Delegates to Strategy Design Pattern (Comprehensive, Third-Party, etc.)
        Double calculatedPremium = calculationService.calculateAnnualPremium(policyType, estimatedValue, cc, year);

        // Find or associate InsurancePackage
        InsurancePackage pkg = packageRepository.findAll().stream().findFirst().orElse(null);

        // 1. Create linked POLICY_APPLICATION record
        PolicyApplication application = new PolicyApplication();
        application.setCustomer(customer);
        application.setVehicle(vehicle);
        application.setInsurancePackage(pkg);
        application.setCalculatedPremium(BigDecimal.valueOf(calculatedPremium));
        application.setApplicationDate(formDto.getStartDate());
        application.setStatus("Approved");
        application.setDecision("Approved");
        application.setDecisionDate(LocalDate.now());
        application.setDecisionComments("Underwritten and approved via Strategy Pattern (" + policyType.name() + ")");

        PolicyApplication savedApp = applicationRepository.save(application);

        // 2. Create linked POLICY record
        InsurancePolicy policy = new InsurancePolicy();
        policy.setApplication(savedApp);
        policy.setPolicyNumber("POL-2026-" + (int)(Math.random() * 9000 + 1000));
        policy.setIssueDate(LocalDate.now());
        policy.setStartDate(formDto.getStartDate());
        policy.setExpiryDate(formDto.getExpiryDate());
        policy.setPremiumAmount(BigDecimal.valueOf(calculatedPremium));
        policy.setPolicyStatus("Active");
        policy.setPolicyType(policyType);
        policy.setSumInsured(estimatedValue);

        InsurancePolicy savedPolicy = policyRepository.save(policy);

        String activeRole = (String) session.getAttribute("activeRole");
        auditService.logAction("staff_user", activeRole, "CREATE_POLICY", "Policy Management",
                "Issued policy via Strategy Pattern: " + savedPolicy.getPolicyNumber() + " with Premium LKR "
                        + String.format("%,.2f", calculatedPremium) + " for Vehicle: " + vehicle.getRegistrationNo());

        redirectAttributes.addFlashAttribute("successMessage", "Policy " + savedPolicy.getPolicyNumber()
                + " issued successfully with Strategy-calculated premium LKR " + String.format("%,.2f", calculatedPremium));
        return "redirect:/policies?success=Policy+created+successfully";
    }

    @PostMapping("/policies/renew/{id}")
    public String renewPolicy(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        InsurancePolicy policy = policyRepository.findById(id).orElse(null);
        if (policy != null) {
            policy.setExpiryDate(policy.getExpiryDate().plusYears(1));
            policy.setRenewalCount(policy.getRenewalCount() + 1);
            policy.setPolicyStatus("Active");
            policyRepository.save(policy);

            String activeRole = (String) session.getAttribute("activeRole");
            auditService.logAction("staff_user", activeRole, "RENEW_POLICY", "Policy Management",
                    "Renewed policy: " + policy.getPolicyNumber() + " for 1 additional year (New expiry: " + policy.getExpiryDate() + ")");
            redirectAttributes.addFlashAttribute("successMessage", "Policy " + policy.getPolicyNumber() + " renewed for 1 additional year.");
        }
        return "redirect:/policies?success=Policy+renewed+successfully";
    }

    @PostMapping("/policies/cancel/{id}")
    public String cancelPolicy(@PathVariable("id") Long id,
                               @RequestParam(value = "reason", defaultValue = "Policyholder Request") String reason,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        InsurancePolicy policy = policyRepository.findById(id).orElse(null);
        if (policy != null) {
            policy.setPolicyStatus("Cancelled");
            policy.setCancellationReason(reason);
            policyRepository.save(policy);

            String activeRole = (String) session.getAttribute("activeRole");
            auditService.logAction("staff_user", activeRole, "CANCEL_POLICY", "Policy Management",
                    "Cancelled policy: " + policy.getPolicyNumber() + " (Reason: " + reason + ")");
            redirectAttributes.addFlashAttribute("successMessage", "Policy " + policy.getPolicyNumber() + " cancelled successfully.");
        }
        return "redirect:/policies?success=Policy+cancelled+successfully";
    }

    @PostMapping("/policies/status/{id}")
    public String updatePolicyStatus(@PathVariable("id") Long id,
                                     @RequestParam("status") String statusStr,
                                     HttpSession session) {
        InsurancePolicy policy = policyRepository.findById(id).orElse(null);
        if (policy != null) {
            policy.setPolicyStatus(statusStr);
            policyRepository.save(policy);

            String activeRole = (String) session.getAttribute("activeRole");
            auditService.logAction("staff_user", activeRole, "UPDATE_POLICY_STATUS", "Policy Management",
                    "Changed policy " + policy.getPolicyNumber() + " status to " + statusStr);
        }
        return "redirect:/policies?success=Policy+status+updated";
    }

    @PostMapping("/policies/delete/{id}")
    public String deletePolicy(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        InsurancePolicy policy = policyRepository.findById(id).orElse(null);
        if (policy != null) {
            String polNo = policy.getPolicyNumber();
            policy.setPolicyStatus("Cancelled");
            policy.setCancellationReason("Record Deactivated / Deleted by Administrator");
            policyRepository.save(policy);

            String activeRole = (String) session.getAttribute("activeRole");
            auditService.logAction("manager", activeRole, "DELETE_POLICY", "Policy Management",
                    "Deleted/Cancelled policy: " + polNo);
            redirectAttributes.addFlashAttribute("successMessage", "Policy " + polNo + " marked cancelled / deactivated.");
        }
        return "redirect:/policies";
    }
}
