package com.insuradrive.controller;

import com.insuradrive.repository.AuditLogRepository;
import com.insuradrive.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AdminController(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/admin/audit-logs")
    public String viewAuditLogs(Model model, HttpSession session) {
        model.addAttribute("auditLogs", auditLogRepository.findAllByOrderByTimestampDesc());
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "admin/audit-logs";
    }

    @GetMapping("/admin/users")
    public String viewUsers(Model model, HttpSession session) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "admin/users";
    }
}
