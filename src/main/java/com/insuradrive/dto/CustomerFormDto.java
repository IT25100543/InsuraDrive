package com.insuradrive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// DTO used to receive and validate customer form data
public class CustomerFormDto {

    private Long id;

    // Customer's full name with length validation
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100,
            message = "Full name must be between 2 and 100 characters")
    private String fullName;

    // Customer email with format and length validation
    @NotBlank(message = "Email address is required")
    @Email(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Please enter a valid email address (e.g. name@domain.com)"
    )
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    // Customer contact number
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9+ -]{9,20}$",
            message = "Please enter a valid phone number (e.g. 0771234567 or +94771234567)"
    )
    private String phone;

    // National Identity Card number
    @NotBlank(message = "National Identity Card (NIC) is required")
    @Size(min = 9, max = 20,
            message = "NIC must be between 9 and 20 characters")
    private String nic;

    // Customer address details
    @NotBlank(message = "Street address is required")
    @Size(max = 100, message = "Street cannot exceed 100 characters")
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City cannot exceed 50 characters")
    private String city;

    @NotBlank(message = "Postal code is required")
    @Size(max = 10, message = "Postal code cannot exceed 10 characters")
    private String postalCode;

    // Additional customer verification information
    private String drivingLicenseNo;
    private String verificationStatus = "VERIFIED";

    public CustomerFormDto() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getDrivingLicenseNo() { return drivingLicenseNo; }
    public void setDrivingLicenseNo(String drivingLicenseNo) {
        this.drivingLicenseNo = drivingLicenseNo;
    }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
}