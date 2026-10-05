package com.insuradrive.service;

import com.insuradrive.model.AuditLog;
import com.insuradrive.model.User;
import com.insuradrive.repository.AuditLogRepository;
import com.insuradrive.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void logAction(User user, String role, String action, String targetModule, String details) {
        try {
            if (user == null) {
                user = userRepository.findById(1L).orElse(null);
            }

            AuditLog log = AuditLog.builder()
                    .user(user)
                    .timestamp(LocalDateTime.now())
                    .username(user != null ? user.getUsername() : "system")
                    .userRole(role != null ? role : "SYSTEM")
                    .action(action)
                    .targetModule(targetModule)
                    .details(details)
                    .build();

            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("AuditService notice: " + e.getMessage());
        }
    }

    @Transactional
    public void logAction(String username, String role, String action, String targetModule, String details) {
        try {
            User user = null;
            if (username != null && !username.trim().isEmpty()) {
                user = userRepository.findByUsername(username.trim()).orElse(null);
            }
            if (user == null) {
                user = userRepository.findById(1L).orElse(null);
            }
            logAction(user, role, action, targetModule, details);
        } catch (Exception e) {
            System.err.println("AuditService notice: " + e.getMessage());
        }
    }
}
