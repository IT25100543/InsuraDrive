package com.insuradrive.service.factory.customer;

import com.insuradrive.model.Customer;
import com.insuradrive.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Concrete factory for creating corporate customer accounts
// Creates the User account and associated Customer profile for corporate or commercial fleet customers, applying corporate-specific
// defaults and account details

@Component
public class CorporateCustomerFactory implements com.insuradrive.service.factory.customer.CustomerFactory {

    private final PasswordEncoder passwordEncoder;

    // Injects the password encoder used to securely hash account passwords
    public CorporateCustomerFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // Creates the User account for a corporate customer
    @Override
    public User createUser(
            String fullName,
            String nic,
            String email,
            String rawPassword
    ) {

        String username =
                (nic != null && !nic.trim().isEmpty())
                        ? "corp_" + nic.trim().toLowerCase()
                        : "corp_" + System.currentTimeMillis();

        String password =
                (rawPassword != null && !rawPassword.trim().isEmpty())
                        ? rawPassword
                        : "password";

        User user = new User();

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        user.setEmail(
                email != null && !email.trim().isEmpty()
                        ? email.trim()
                        : username + "@fleet.insuradrive.com"
        );

        user.setFullName(
                fullName != null && !fullName.trim().isEmpty()
                        ? fullName.trim() + " (Corporate)"
                        : "Corporate Account"
        );

        user.setRole(User.Role.CUSTOMER);
        user.setAccountStatus("Active");

        return user;
    }

    // Creates the Customer profile associated with the corporate User account
    @Override
    public Customer createCustomer(
            User user,
            String fullName,
            String nic,
            String phone,
            String address,
            String drivingLicenseNo,
            String documentName
    ) {

        Customer customer = new Customer();

        customer.setUser(user);

        // Uses the same user ID when the associated User has already been saved
        if (user != null && user.getUserID() != null) {
            customer.setUserID(user.getUserID());
        }

        customer.setNic(
                nic != null
                        ? nic.trim()
                        : ""
        );

        customer.setPhoneNumber(
                phone != null && !phone.trim().isEmpty()
                        ? phone.trim()
                        : "0112000000"
        );

        customer.setPhone(customer.getPhoneNumber());

        customer.setFullName(
                fullName != null
                        ? fullName.trim()
                        : ""
        );

        customer.setEmail(
                user != null
                        ? user.getEmail()
                        : ""
        );

        customer.setAddress(
                address != null && !address.trim().isEmpty()
                        ? address.trim()
                        : "Corporate Tower, Level 4, Colombo 02, 00200"
        );

        customer.setDrivingLicenseNo(
                drivingLicenseNo != null && !drivingLicenseNo.trim().isEmpty()
                        ? drivingLicenseNo.trim()
                        : "CORP-FLEET-AUTH"
        );

        customer.setDocumentName(
                documentName != null && !documentName.trim().isEmpty()
                        ? documentName.trim()
                        : "business_registration_form20.pdf"
        );

        // Corporate customers are currently created as verified by default.
        customer.setVerificationStatus(
                Customer.VerificationStatus.VERIFIED
        );

        customer.setRegisteredAt(LocalDateTime.now());

        return customer;
    }
}