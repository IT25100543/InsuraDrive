package com.insuradrive.model;

import jakarta.persistence.*;


@Entity
@Table(name = "STAFF")
public class Staff {

    @Id
    @Column(name = "userID")
    private Long userID;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "userID")
    private User user;

    @Column(name = "employeeNo", length = 20, nullable = false, unique = true)
    private String employeeNo;

    @Column(name = "staffStatus", length = 20, nullable = false)
    private String staffStatus = "Active";

    @Column(name = "staffRole", length = 50, nullable = false)
    private String staffRole;

    public Staff() {}

    public Staff(Long userID, User user, String employeeNo, String staffStatus, String staffRole) {
        this.userID = userID;
        this.user = user;
        this.employeeNo = employeeNo;
        this.staffStatus = staffStatus != null ? staffStatus : "Active";
        this.staffRole = staffRole;
    }

    public static StaffBuilder builder() {
        return new StaffBuilder();
    }

    public static class StaffBuilder {
        private Long userID;
        private User user;
        private String employeeNo;
        private String staffStatus = "Active";
        private String staffRole;

        public StaffBuilder userID(Long userID) { this.userID = userID; return this; }
        public StaffBuilder userID(Integer userID) { this.userID = userID != null ? userID.longValue() : null; return this; }
        public StaffBuilder user(User user) { this.user = user; return this; }
        public StaffBuilder employeeNo(String employeeNo) { this.employeeNo = employeeNo; return this; }
        public StaffBuilder staffStatus(String staffStatus) { this.staffStatus = staffStatus; return this; }
        public StaffBuilder staffRole(String staffRole) { this.staffRole = staffRole; return this; }

        public Staff build() {
            return new Staff(userID, user, employeeNo, staffStatus, staffRole);
        }
    }

    public Long getUserID() { return userID; }
    public void setUserID(Long userID) { this.userID = userID; }
    public void setUserID(Integer userID) { this.userID = userID != null ? userID.longValue() : null; }

    public Long getId() { return userID; }
    public void setId(Long id) { this.userID = id; }
    public void setId(Integer id) { this.userID = id != null ? id.longValue() : null; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public String getStaffStatus() { return staffStatus; }
    public void setStaffStatus(String staffStatus) { this.staffStatus = staffStatus; }

    public String getStaffRole() { return staffRole; }
    public void setStaffRole(String staffRole) { this.staffRole = staffRole; }
}
