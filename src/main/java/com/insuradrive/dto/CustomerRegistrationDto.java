package com.insuradrive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// DTO used to receive and validate customer registration data
public class CustomerRegistrationDto {

    // Account credentials
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9._-]+$",
            message = "Username can only contain letters, numbers, dots, underscores, and hyphens"
    )
    private String username;

    @NotBlank(message = "Email is required")
    @Email(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Please enter a valid email address (e.g. name@domain.com)"
    )
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    // Customer identification details
    @NotBlank(message = "National Identity Card (NIC) is required")
    @Size(min = 9, max = 20, message = "NIC must be between 9 and 20 characters")
    private String nic;

    // Customer name details
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Middle name cannot exceed 50 characters")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    // Contact information
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9+ -]{9,20}$",
            message = "Please enter a valid phone number (e.g. 0771234567 or +94771234567)"
    )
    private String phoneNumber;

    // Customer address details
    @NotBlank(message = "Street address is required")
    @Size(max = 100, message = "Street address cannot exceed 100 characters")
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City cannot exceed 50 characters")
    private String city;

    @NotBlank(message = "Postal code is required")
    @Size(max = 10, message = "Postal code cannot exceed 10 characters")
    private String postalCode;

    public CustomerRegistrationDto() {}

    // Getters and setters
}