package com.insuradrive.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "[USER]")
public class User {

    public enum Role {
        CUSTOMER,
        CUSTOMER_RELATIONS_OFFICER,
        CLAIMS_EXECUTIVE,
        FINANCE_SUPERVISOR,
        RISK_COMPLIANCE_OFFICER,
        BRANCH_MANAGER,
        ADMIN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userID")
    private Long userID;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "accountStatus", length = 20, nullable = false)
    private String accountStatus = "Active";

    @Column(name = "createdDate", insertable = false, updatable = false)
    private LocalDateTime createdDate;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Customer customer;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Staff staff;

    @Transient
    private String fullName;

    @Transient
    private Role role;

    public User() {}

    public User(Long userID, String username, String password, String email, String accountStatus) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.email = email;
        this.accountStatus = accountStatus != null ? accountStatus : "Active";
    }

    public User(Long userID, String username, String password, String email, String fullName, Role role, String accountStatus) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.accountStatus = accountStatus != null ? accountStatus : "Active";
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long userID;
        private String username;
        private String password;
        private String email;
        private String fullName;
        private Role role;
        private String accountStatus = "Active";

        public UserBuilder userID(Long userID) { this.userID = userID; return this; }
        public UserBuilder userID(Integer userID) { this.userID = userID != null ? userID.longValue() : null; return this; }
        public UserBuilder id(Long id) { this.userID = id; return this; }
        public UserBuilder id(Integer id) { this.userID = id != null ? id.longValue() : null; return this; }
        public UserBuilder username(String username) { this.username = username; return this; }
        public UserBuilder password(String password) { this.password = password; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserBuilder role(Role role) { this.role = role; return this; }
        public UserBuilder accountStatus(String accountStatus) { this.accountStatus = accountStatus; return this; }

        public User build() {
            return new User(userID, username, password, email, fullName, role, accountStatus);
        }
    }

    // Convenience Getters / Setters
    public Long getUserID() { return userID; }
    public void setUserID(Long userID) { this.userID = userID; }
    public void setUserID(Integer userID) { this.userID = userID != null ? userID.longValue() : null; }

    public Long getId() { return userID; }
    public void setId(Long id) { this.userID = id; }
    public void setId(Integer id) { this.userID = id != null ? id.longValue() : null; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Staff getStaff() { return staff; }
    public void setStaff(Staff staff) { this.staff = staff; }

    public String getFullName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        if (customer != null && customer.getFullName() != null && !customer.getFullName().trim().isEmpty()) {
            return customer.getFullName();
        }
        if (staff != null && staff.getStaffRole() != null) {
            return username + " (" + staff.getStaffRole() + ")";
        }
        return username;
    }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() {
        if (role != null) return role;
        if (staff != null) {
            String sr = staff.getStaffRole();
            if (sr != null) {
                switch (sr.trim()) {
                    case "Customer Relations Officer": return Role.CUSTOMER_RELATIONS_OFFICER;
                    case "Branch Operations Manager": return Role.BRANCH_MANAGER;
                    case "System Administrator": return Role.ADMIN;
                    case "Risk and Compliance Officer": return Role.RISK_COMPLIANCE_OFFICER;
                    case "Claims Processing Executive": return Role.CLAIMS_EXECUTIVE;
                    case "Finance and Payment Supervisor": return Role.FINANCE_SUPERVISOR;
                }
            }
        }
        if (customer != null) return Role.CUSTOMER;
        if (username != null) {
            String u = username.toLowerCase();
            if (u.contains("manager")) return Role.BRANCH_MANAGER;
            if (u.contains("admin")) return Role.ADMIN;
            if (u.contains("cro")) return Role.CUSTOMER_RELATIONS_OFFICER;
            if (u.contains("claim")) return Role.CLAIMS_EXECUTIVE;
            if (u.contains("finance")) return Role.FINANCE_SUPERVISOR;
            if (u.contains("risk") || u.contains("compliance")) return Role.RISK_COMPLIANCE_OFFICER;
        }
        return Role.CUSTOMER;
    }
    public void setRole(Role role) { this.role = role; }

    public String getDisplayName() {
        if (customer != null && customer.getFullName() != null && !customer.getFullName().isEmpty()) {
            return customer.getFullName();
        } else if (staff != null) {
            return username + " (" + staff.getStaffRole() + ")";
        }
        return fullName != null ? fullName : username;
    }

    public String getRoleName() {
        Role r = getRole();
        return r != null ? r.name() : "USER";
    }
}
