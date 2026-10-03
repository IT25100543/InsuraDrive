package com.insuradrive.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Customer entity associated with a User account

@Entity

// Maps the Customer class to the CUSTOMER table in the database
@Table(name = "CUSTOMER")

public class Customer {

    // Possible verification states of a customer
    public enum VerificationStatus {
        VERIFIED,
        PENDING,
        PENDING_VERIFICATION,
        REJECTED
    }

    // Primary key of the customer, shared with the associated User entity
    @Id
    @Column(name = "userID")
    private Long userID;

    // One-to-one relationship with User using the same userID
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "userID")
    private User user;

    // Customer personal and contact information
    @Column(name = "NIC", length = 20, nullable = false, unique = true)
    private String nic;

    @Column(name = "phoneNumber", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "firstName", length = 50, nullable = false)
    private String firstName;

    @Column(name = "middleName", length = 50)
    private String middleName;

    @Column(name = "lastName", length = 50, nullable = false)
    private String lastName;

    @Column(name = "street", length = 100, nullable = false)
    private String street;

    @Column(name = "city", length = 50, nullable = false)
    private String city;

    @Column(name = "postalCode", length = 10, nullable = false)
    private String postalCode;


    // Relationships to vehicles, uploaded documents, and policy applications
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vehicle> vehicles = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PolicyApplication> applications = new ArrayList<>();


    // Non-persistent helper fields used by the application/UI
    @Transient
    private String fullName;

    @Transient
    private String email;

    @Transient
    private String phone;

    @Transient
    private String address;

    @Transient
    private String drivingLicenseNo = "B" + (int)(Math.random() * 9000000 + 1000000);

    @Enumerated(EnumType.STRING)
    @Transient
    private VerificationStatus verificationStatus = VerificationStatus.VERIFIED;

    @Transient
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Transient
    private String documentName;

    public Customer() {}

    // Creates a customer with the main database-backed customer details
    public Customer(Long userID, User user, String nic, String phoneNumber, String firstName,
                    String middleName, String lastName, String street, String city, String postalCode) {
        this.userID = userID;
        this.user = user;
        this.nic = nic;
        this.phoneNumber = phoneNumber;
        this.phone = phoneNumber;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
    }

    @PrePersist
    @PreUpdate

    // Ensures that every Customer has an associated User before saving or updating
    public void ensureUser() {
        if (this.user == null) {
            String uname = this.nic != null ? this.nic : "user_" + System.currentTimeMillis();
            String umail = (this.email != null && !this.email.isEmpty()) ? this.email : (uname + "@insuradrive.com");
            this.user = User.builder()
                    .username(uname)
                    .password("$2a$10$wN1r7G5X15D0yUjRjLzO0e9b9c9f8a7e6d5c4b3a2")
                    .email(umail)
                    .fullName(getFullName())
                    .role(User.Role.CUSTOMER)
                    .accountStatus("Active")
                    .build();
        }
    }

    // Builder for creating Customer objects using a readable chained syntax
    public static CustomerBuilder builder() {
        return new CustomerBuilder();
    }

    public static class CustomerBuilder {
        private Long userID;
        private User user;
        private String nic;
        private String phoneNumber;
        private String firstName = "John";
        private String middleName;
        private String lastName = "Doe";
        private String street = "Main Street";
        private String city = "Colombo";
        private String postalCode = "00100";
        private String fullName;
        private String email;
        private String drivingLicenseNo = "B" + (int)(Math.random() * 9000000 + 1000000);
        private VerificationStatus verificationStatus = VerificationStatus.VERIFIED;
        private LocalDateTime registeredAt = LocalDateTime.now();
        private String documentName;

        public CustomerBuilder userID(Long userID) { this.userID = userID; return this; }
        public CustomerBuilder userID(Integer userID) { this.userID = userID != null ? userID.longValue() : null; return this; }
        public CustomerBuilder id(Long id) { this.userID = id; return this; }
        public CustomerBuilder id(Integer id) { this.userID = id != null ? id.longValue() : null; return this; }
        public CustomerBuilder user(User user) { this.user = user; return this; }
        public CustomerBuilder nic(String nic) { this.nic = nic; return this; }
        public CustomerBuilder nicPassport(String nicPassport) { this.nic = nicPassport; return this; }
        public CustomerBuilder phoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; return this; }
        public CustomerBuilder phone(String phone) { this.phoneNumber = phone; return this; }
        public CustomerBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public CustomerBuilder middleName(String middleName) { this.middleName = middleName; return this; }
        public CustomerBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public CustomerBuilder street(String street) { this.street = street; return this; }
        public CustomerBuilder city(String city) { this.city = city; return this; }
        public CustomerBuilder postalCode(String postalCode) { this.postalCode = postalCode; return this; }
        public CustomerBuilder address(String address) {
            if (address != null && !address.trim().isEmpty()) {
                String[] parts = address.split(",");
                this.street = parts[0].trim();
                if (parts.length > 1) this.city = parts[1].trim();
                if (parts.length > 2) this.postalCode = parts[2].trim();
            }
            return this;
        }

        public CustomerBuilder fullName(String fullName) {
            this.fullName = fullName;
            if (fullName != null && !fullName.trim().isEmpty()) {
                String[] parts = fullName.trim().split("\\s+");

                if (parts.length == 1) {
                    this.firstName = parts[0];
                    this.lastName = parts[0];
                } else if (parts.length == 2) {
                    this.firstName = parts[0];
                    this.lastName = parts[1];
                } else {
                    this.firstName = parts[0];
                    this.middleName = parts[1];
                    this.lastName = parts[parts.length - 1];
                }
            }
            return this;
        }
        public CustomerBuilder email(String email) { this.email = email; return this; }
        public CustomerBuilder drivingLicenseNo(String drivingLicenseNo) { this.drivingLicenseNo = drivingLicenseNo; return this; }
        public CustomerBuilder verificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; return this; }
        public CustomerBuilder registeredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; return this; }
        public CustomerBuilder documentName(String documentName) { this.documentName = documentName; return this; }

        public Customer build() {
            Customer c = new Customer(userID, user, nic, phoneNumber, firstName, middleName, lastName, street, city, postalCode);
            c.setFullName(fullName);
            c.setEmail(email);
            c.setPhone(phoneNumber);
            c.setDrivingLicenseNo(drivingLicenseNo);
            c.setVerificationStatus(verificationStatus);
            c.setRegisteredAt(registeredAt);
            c.setDocumentName(documentName);
            c.ensureUser();
            return c;
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

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public String getNicPassport() { return nic; }
    public void setNicPassport(String nicPassport) { this.nic = nicPassport; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; this.phone = phoneNumber; }

    public String getPhone() { return phone != null ? phone : phoneNumber; }
    public void setPhone(String phone) { this.phone = phone; this.phoneNumber = phone; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public List<Vehicle> getVehicles() { return vehicles; }
    public void setVehicles(List<Vehicle> vehicles) { this.vehicles = vehicles; }

    public List<CustomerDocument> getDocuments() { return documents; }
    public void setDocuments(List<CustomerDocument> documents) { this.documents = documents; }

    public List<PolicyApplication> getApplications() { return applications; }
    public void setApplications(List<PolicyApplication> applications) { this.applications = applications; }

    // Builds the customer's display name from individual name fields
    public String getFullName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        if (firstName != null && lastName != null) {
            if (middleName != null && !middleName.trim().isEmpty()) {
                return firstName + " " + middleName.trim() + " " + lastName;
            }
            return firstName + " " + lastName;
        }
        if (firstName != null) return firstName;
        return "";
    }

    // Splits a full name into first, middle, and last name components
    public void setFullName(String fullName) {
        this.fullName = fullName;
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+");
            if (parts.length == 1) {
                this.firstName = parts[0];
                this.lastName = parts[0];
            } else if (parts.length == 2) {
                this.firstName = parts[0];
                this.lastName = parts[1];
            } else {
                this.firstName = parts[0];
                this.middleName = parts[1];
                this.lastName = parts[parts.length - 1];
            }
        }
    }

    public String getAddress() {
        if (address != null && !address.trim().isEmpty()) return address;
        return getFullAddress();
    }

    // Splits a formatted address into individual address fields
    public void setAddress(String address) {
        this.address = address;
        if (address != null && !address.trim().isEmpty()) {
            String[] parts = address.split(",");
            this.street = parts[0].trim();
            if (parts.length > 1) this.city = parts[1].trim();
            if (parts.length > 2) this.postalCode = parts[2].trim();
        }
    }

    // Returns the customer's complete formatted address
    public String getFullAddress() {
        if (street != null && city != null) {
            return street + ", " + city + (postalCode != null ? " " + postalCode : "");
        }
        return street != null ? street : "";
    }

    // Returns the customer email, falling back to the associated User email
    public String getEmail() {
        if (email != null && !email.isEmpty()) return email;
        return user != null ? user.getEmail() : "";
    }

    public void setEmail(String email) {
        this.email = email;
        if (user != null) {
            user.setEmail(email);
        }
    }

    public String getDrivingLicenseNo() { return drivingLicenseNo; }
    public void setDrivingLicenseNo(String drivingLicenseNo) { this.drivingLicenseNo = drivingLicenseNo; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    // Checks whether the customer or at least one submitted document is verified
    public boolean isVerified() {
        if (verificationStatus == VerificationStatus.VERIFIED) return true;
        if (documents == null || documents.isEmpty()) return false;
        return documents.stream().anyMatch(d -> "Verified".equalsIgnoreCase(d.getVerificationStatus()));
    }
}
