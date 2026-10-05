package com.insuradrive.service;

import com.insuradrive.dto.CustomerRegistrationDto;
import com.insuradrive.model.Customer;
import com.insuradrive.model.User;
import com.insuradrive.repository.CustomerRepository;
import com.insuradrive.repository.UserRepository;
import com.insuradrive.service.factory.customer.CustomerAccountType;
import com.insuradrive.service.factory.customer.CustomerFactory;
import com.insuradrive.service.factory.customer.CustomerFactoryProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Uses the Customer Factory Pattern to create the related User and Customer entities consistently

@Service
public class CustomerRegistrationService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerFactoryProducer factoryProducer;
    private final AuditService auditService;

    // Injects repositories, factory selector, and audit service
    public CustomerRegistrationService(
            UserRepository userRepository,
            CustomerRepository customerRepository,
            CustomerFactoryProducer factoryProducer,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.factoryProducer = factoryProducer;
        this.auditService = auditService;
    }

    // Registers a new individual customer account
    // Validates duplicate account data, creates the User and Customer through the factory, persists both entities, and records the action
    @Transactional
    public Customer registerCustomer(CustomerRegistrationDto dto) {

        // Validate password confirmation
        if (dto.getPassword() == null
                || !dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException(
                    "Passwords do not match. Please re-enter."
            );
        }

        // Check whether the requested username already exists.
        String username = dto.getUsername().trim();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username '" + username
                            + "' is already in use. Please choose another."
            );
        }

        // Check whether the email address is already registered
        String email = dto.getEmail().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email address '" + email + "' is already registered."
            );
        }

        // Check whether the NIC is already registered
        String nic = dto.getNic().trim();

        if (customerRepository.existsByNic(nic)
                || customerRepository.existsByNicPassport(nic)) {

            throw new IllegalArgumentException(
                    "National Identity Card (NIC) '"
                            + nic
                            + "' is already registered."
            );
        }

        // Construct the customer's full name
        StringBuilder nameBuilder =
                new StringBuilder(dto.getFirstName().trim());

        if (dto.getMiddleName() != null
                && !dto.getMiddleName().trim().isEmpty()) {

            nameBuilder
                    .append(" ")
                    .append(dto.getMiddleName().trim());
        }

        nameBuilder
                .append(" ")
                .append(dto.getLastName().trim());

        String fullName = nameBuilder.toString();

        // Select the factory used for individual customer registration
        CustomerFactory factory =
                factoryProducer.getFactory(CustomerAccountType.INDIVIDUAL);

        // Create and save the User entity
        User user = factory.createUser(
                fullName,
                nic,
                email,
                dto.getPassword()
        );

        // Preserve the username entered in the registration form
        user.setUsername(username);

        User savedUser = userRepository.save(user);

        // Build the customer's formatted address
        String fullAddress =
                dto.getStreet().trim()
                        + ", "
                        + dto.getCity().trim()
                        + ", "
                        + dto.getPostalCode().trim();

        // Create the Customer profile through the selected factory
        Customer customer = factory.createCustomer(
                savedUser,
                fullName,
                nic,
                dto.getPhoneNumber().trim(),
                fullAddress,
                null,
                "nic_scan_verified.pdf"
        );

        customer.setUserID(savedUser.getUserID());

        // Store the individual customer name fields
        customer.setFirstName(dto.getFirstName().trim());

        if (dto.getMiddleName() != null
                && !dto.getMiddleName().trim().isEmpty()) {

            customer.setMiddleName(dto.getMiddleName().trim());
        }

        customer.setLastName(dto.getLastName().trim());

        // Store the individual address fields
        customer.setStreet(dto.getStreet().trim());
        customer.setCity(dto.getCity().trim());
        customer.setPostalCode(dto.getPostalCode().trim());

        // Save the completed customer profile
        Customer savedCustomer =
                customerRepository.save(customer);

        // Link the saved customer back to the User entity
        savedUser.setCustomer(savedCustomer);

        // Record the successful registration action
        auditService.logAction(
                username,
                "CUSTOMER",
                "REGISTER_ACCOUNT",
                "Customer Self-Service",
                "Customer registered via IndividualCustomerFactory: "
                        + fullName
                        + " (NIC: "
                        + nic
                        + ")"
        );

        return savedCustomer;
    }
}