package com.insuradrive.controller;

import com.insuradrive.model.User;
import com.insuradrive.repository.*;
import com.insuradrive.service.AuditService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

    private final com.insuradrive.repository.CustomerRepository customerRepository;
    private final com.insuradrive.repository.VehicleRepository vehicleRepository;
    private final com.insuradrive.repository.InsurancePolicyRepository policyRepository;
    private final com.insuradrive.repository.ClaimRepository claimRepository;
    private final com.insuradrive.repository.PaymentRepository paymentRepository;
    private final com.insuradrive.repository.AuditLogRepository auditLogRepository;
    private final AuditService auditService;

    public MainController(com.insuradrive.repository.CustomerRepository customerRepository,
                          com.insuradrive.repository.VehicleRepository vehicleRepository,
                          com.insuradrive.repository.InsurancePolicyRepository policyRepository,
                          com.insuradrive.repository.ClaimRepository claimRepository,
                          com.insuradrive.repository.PaymentRepository paymentRepository,
                          com.insuradrive.repository.AuditLogRepository auditLogRepository,
                          AuditService auditService) {
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.paymentRepository = paymentRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/switch-role")
    public String switchRole(@RequestParam("role") String roleStr, HttpSession session, org.springframework.security.core.Authentication auth) {
        // Prevent pure customers from switching to staff roles
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            boolean isStaffOrAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_BRANCH_MANAGER"));
            if (!isStaffOrAdmin) {
                session.setAttribute("activeRole", User.Role.CUSTOMER.name());
                return "redirect:/customer/dashboard";
            }
        }

        try {
            User.Role role = User.Role.valueOf(roleStr);
            session.setAttribute("activeRole", role.name());
            auditService.logAction(auth != null ? auth.getName() : "user", role.name(), "SWITCH_ROLE", "Session", "Switched role view to " + role.name());
        } catch (Exception e) {
            session.setAttribute("activeRole", User.Role.CUSTOMER.name());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, org.springframework.security.core.Authentication auth) {
        String activeRole = (String) session.getAttribute("activeRole");
        if (activeRole == null && auth != null && auth.isAuthenticated()) {
            if (auth.getPrincipal() instanceof com.insuradrive.service.CustomUserDetails userDetails) {
                activeRole = userDetails.getUser().getRole().name();
            } else if (auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
                String authority = auth.getAuthorities().iterator().next().getAuthority();
                if (authority.startsWith("ROLE_")) {
                    activeRole = authority.substring(5);
                }
            }
            if (activeRole != null) {
                session.setAttribute("activeRole", activeRole);
            }
        }
        if (activeRole == null) {
            activeRole = User.Role.BRANCH_MANAGER.name();
            session.setAttribute("activeRole", activeRole);
        }

        if (User.Role.CUSTOMER.name().equalsIgnoreCase(activeRole)) {
            return "redirect:/customer/dashboard";
        }

        model.addAttribute("activeRole", activeRole);
        model.addAttribute("totalCustomers", customerRepository.count());
        model.addAttribute("totalVehicles", vehicleRepository.count());
        model.addAttribute("totalPolicies", policyRepository.count());
        model.addAttribute("totalClaims", claimRepository.count());
        model.addAttribute("totalPayments", paymentRepository.count());

        switch (User.Role.valueOf(activeRole)) {
            case CUSTOMER:
                return "redirect:/customer/dashboard";

            case CUSTOMER_RELATIONS_OFFICER:
                model.addAttribute("recentCustomers", customerRepository.findAll());
                model.addAttribute("recentVehicles", vehicleRepository.findAll());
                return "dashboard/cro";

            case CLAIMS_EXECUTIVE:
                model.addAttribute("pendingClaims", claimRepository.findAll());
                return "dashboard/claims";

            case FINANCE_SUPERVISOR:
                model.addAttribute("recentPayments", paymentRepository.findAll());
                model.addAttribute("policies", policyRepository.findAll());
                return "dashboard/finance";

            case RISK_COMPLIANCE_OFFICER:
                model.addAttribute("auditLogs", auditLogRepository.findAllByOrderByTimestampDesc());
                return "dashboard/compliance";

            case BRANCH_MANAGER:
            case ADMIN:
            default:
                model.addAttribute("recentPolicies", policyRepository.findAll());
                model.addAttribute("recentClaims", claimRepository.findAll());
                model.addAttribute("recentAuditLogs", auditLogRepository.findAllByOrderByTimestampDesc());
                return "dashboard/branch-manager";
        }
    }
}
