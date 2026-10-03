package com.insuradrive.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Maps to table AUDIT_LOG in InsuraDrive_DDD.
 * Automatically populated via trigger trg_UserAccountCreated and service activity calls.
 */
@Entity
@Table(name = "AUDIT_LOG")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logID")
    private Long logID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userID", nullable = true)
    private User user;

    @Column(name = "activity", length = 255, nullable = false)
    private String activity;

    @Column(name = "activityDateTime")
    private LocalDateTime activityDateTime = LocalDateTime.now();

    @Transient
    private String username;

    @Transient
    private String userRole;

    @Transient
    private String action;

    @Transient
    private String targetModule;

    @Transient
    private String details;

    public AuditLog() {}

    public AuditLog(Long logID, User user, String activity, LocalDateTime activityDateTime) {
        this.logID = logID;
        this.user = user;
        this.activity = activity;
        this.activityDateTime = activityDateTime != null ? activityDateTime : LocalDateTime.now();
    }

    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private Long logID;
        private User user;
        private String activity;
        private LocalDateTime activityDateTime = LocalDateTime.now();
        private String username;
        private String userRole;
        private String action;
        private String targetModule;
        private String details;

        public AuditLogBuilder logID(Long logID) { this.logID = logID; return this; }
        public AuditLogBuilder logID(Integer logID) { this.logID = logID != null ? logID.longValue() : null; return this; }
        public AuditLogBuilder id(Long id) { this.logID = id; return this; }
        public AuditLogBuilder id(Integer id) { this.logID = id != null ? id.longValue() : null; return this; }
        public AuditLogBuilder user(User user) { this.user = user; return this; }
        public AuditLogBuilder activity(String activity) { this.activity = activity; return this; }
        public AuditLogBuilder activityDateTime(LocalDateTime activityDateTime) { this.activityDateTime = activityDateTime; return this; }
        public AuditLogBuilder timestamp(LocalDateTime timestamp) { this.activityDateTime = timestamp; return this; }
        public AuditLogBuilder username(String username) { this.username = username; return this; }
        public AuditLogBuilder userRole(String userRole) { this.userRole = userRole; return this; }
        public AuditLogBuilder action(String action) { this.action = action; return this; }
        public AuditLogBuilder targetModule(String targetModule) { this.targetModule = targetModule; return this; }
        public AuditLogBuilder details(String details) { this.details = details; return this; }

        public AuditLog build() {
            String act = activity;
            if (act == null || act.trim().isEmpty()) {
                if (action != null && details != null) {
                    act = action + ": " + details;
                } else if (details != null) {
                    act = details;
                } else if (action != null) {
                    act = action;
                } else {
                    act = "System event";
                }
            }
            if (act.length() > 255) {
                act = act.substring(0, 255);
            }
            AuditLog log = new AuditLog(logID, user, act, activityDateTime != null ? activityDateTime : LocalDateTime.now());
            log.setUsername(username);
            log.setUserRole(userRole);
            log.setAction(action);
            log.setTargetModule(targetModule);
            log.setDetails(details != null ? details : act);
            return log;
        }
    }

    public Long getLogID() { return logID; }
    public void setLogID(Long logID) { this.logID = logID; }
    public void setLogID(Integer logID) { this.logID = logID != null ? logID.longValue() : null; }

    public Long getId() { return logID; }
    public void setId(Long id) { this.logID = id; }
    public void setId(Integer id) { this.logID = id != null ? id.longValue() : null; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public LocalDateTime getActivityDateTime() { return activityDateTime; }
    public void setActivityDateTime(LocalDateTime activityDateTime) { this.activityDateTime = activityDateTime; }

    public LocalDateTime getTimestamp() { return activityDateTime; }
    public void setTimestamp(LocalDateTime timestamp) { this.activityDateTime = timestamp; }

    public String getUsername() {
        if (username != null && !username.trim().isEmpty()) return username;
        return user != null ? user.getUsername() : "System";
    }
    public void setUsername(String username) { this.username = username; }

    public String getUserRole() {
        if (userRole != null && !userRole.trim().isEmpty()) return userRole;
        return user != null ? user.getRoleName() : "SYSTEM";
    }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getAction() {
        if (action != null && !action.trim().isEmpty()) return action;
        return activity != null ? activity : "ACTION";
    }
    public void setAction(String action) { this.action = action; }

    public String getTargetModule() {
        if (targetModule != null && !targetModule.trim().isEmpty()) return targetModule;
        return "System";
    }
    public void setTargetModule(String targetModule) { this.targetModule = targetModule; }

    public String getDetails() {
        if (details != null && !details.trim().isEmpty()) return details;
        return activity;
    }
    public void setDetails(String details) { this.details = details; }
}
