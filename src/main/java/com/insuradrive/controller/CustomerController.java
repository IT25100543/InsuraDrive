package com.insuradrive.controller;

import com.insuradrive.dto.CustomerFormDto;
import com.insuradrive.model.Customer;
import com.insuradrive.model.User;
import com.insuradrive.repository.CustomerRepository;
import com.insuradrive.repository.UserRepository;
import com.insuradrive.service.AuditService;
import com.insuradrive.service.factory.customer.CustomerAccountType;
import com.insuradrive.service.factory.customer.CustomerFactory;
import com.insuradrive.service.factory.customer.CustomerFactoryProducer;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CustomerFactoryProducer factoryProducer;
    private final AuditService auditService;

    // Injects repositories, factory producer, and audit service
    public CustomerController(
            CustomerRepository customerRepository,
            UserRepository userRepository,
            CustomerFactoryProducer factoryProducer,
            AuditService auditService
    ) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.factoryProducer = factoryProducer;
        this.auditService = auditService;
    }

    // Displays all customers and supports name-based searching
    @GetMapping("/customers")
    public String listCustomers(
            @RequestParam(value = "search", required = false) String search,
            Model model,
            HttpSession session
    ) {

        List<Customer> customers;

        if (search != null && !search.trim().isEmpty()) {
            customers =
                    customerRepository
                            .findByFullNameContainingIgnoreCase(search.trim());
        } else {
            customers = customerRepository.findAll();
        }

        model.addAttribute("customers", customers);
        model.addAttribute("searchQuery", search);
        model.addAttribute(
                "activeRole",
                session.getAttribute("activeRole")
        );

        return "customers/list";
    }

    // Displays the form used to register a new customer
    @GetMapping("/customers/new")
    public String newCustomerForm(
            Model model,
            HttpSession session
    ) {

        if (!model.containsAttribute("customer")) {
            model.addAttribute(
                    "customer",
                    new CustomerFormDto()
            );
        }

        model.addAttribute(
                "activeRole",
                session.getAttribute("activeRole")
        );

        return "customers/form";
    }

    // Validates and saves a new customer using the Customer Factory Pattern
    @PostMapping("/customers/save")
    public String saveCustomer(
            @Valid @ModelAttribute("customer") CustomerFormDto formDto,
            BindingResult bindingResult,
            @RequestParam(value = "documentFile", required = false) String docName,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        String nic =
                formDto.getNic() != null
                        ? formDto.getNic().trim()
                        : "";

        String email =
                formDto.getEmail() != null
                        ? formDto.getEmail().trim()
                        : "";

        // Check whether the NIC has already been registered
        if (customerRepository.existsByNic(nic)
                || customerRepository.existsByNicPassport(nic)) {

            bindingResult.rejectValue(
                    "nic",
                    "error.nic",
                    "A customer with this National Identity Card (NIC) "
                            + "is already registered."
            );
        }

        // Check whether the email address is already in use
        if (userRepository.existsByEmail(email)) {

            bindingResult.rejectValue(
                    "email",
                    "error.email",
                    "This email address is already registered in the system."
            );
        }

        // Return to the registration form if validation fails
        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "activeRole",
                    session.getAttribute("activeRole")
            );

            return "customers/form";
        }

        // Select the factory for creating an individual customer
        CustomerFactory factory =
                factoryProducer.getFactory(
                        CustomerAccountType.INDIVIDUAL
                );

        // Create and save the User entity through the factory
        User user = factory.createUser(
                formDto.getFullName(),
                nic,
                email,
                "password"
        );

        User savedUser =
                userRepository.save(user);

        // Build the customer's formatted full address
        String fullAddress =
                formDto.getStreet().trim()
                        + ", "
                        + formDto.getCity().trim()
                        + ", "
                        + formDto.getPostalCode().trim();

        // Create the Customer entity linked to the saved User
        Customer customer =
                factory.createCustomer(
                        savedUser,
                        formDto.getFullName(),
                        nic,
                        formDto.getPhone().trim(),
                        fullAddress,
                        formDto.getDrivingLicenseNo(),
                        docName
                );

        customer.setUserID(savedUser.getUserID());

        // Extract first and last names from the provided full name
        customer.setFirstName(
                formDto.getFullName()
                        .split("\\s+")[0]
        );

        customer.setLastName(
                formDto.getFullName().contains(" ")
                        ? formDto.getFullName().substring(
                        formDto.getFullName()
                                .lastIndexOf(" ") + 1
                )
                        : formDto.getFullName()
        );

        // Store the individual address fields
        customer.setStreet(
                formDto.getStreet().trim()
        );

        customer.setCity(
                formDto.getCity().trim()
        );

        customer.setPostalCode(
                formDto.getPostalCode().trim()
        );

        // Save the completed Customer entity
        Customer saved =
                customerRepository.save(customer);

        // Link the saved Customer back to the User entity
        savedUser.setCustomer(saved);

        // Record the registration action in the audit log
        String activeRole =
                (String) session.getAttribute("activeRole");

        auditService.logAction(
                "staff_user",
                activeRole,
                "REGISTER_CUSTOMER",
                "Customer Management",
                "Enrolled customer via Factory Pattern: "
                        + saved.getFullName()
                        + " (NIC: "
                        + saved.getNicPassport()
                        + ")"
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Customer registered successfully into SQL Server database!"
        );

        return "redirect:/customers?success=Customer+registered+successfully";
    }

    // Loads an existing customer record for editing
    @GetMapping("/customers/edit/{id}")
    public String editCustomerForm(
            @PathVariable("id") Long id,
            Model model,
            HttpSession session
    ) {

        Customer customer =
                customerRepository
                        .findById(id)
                        .orElse(null);

        // Redirect if the requested customer does not exist
        if (customer == null) {
            return "redirect:/customers?error=Customer+not+found";
        }

        // Convert the Customer entity into a DTO for the edit form
        CustomerFormDto dto =
                new CustomerFormDto();

        dto.setId(customer.getUserID());
        dto.setFullName(customer.getFullName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhoneNumber());
        dto.setNic(customer.getNic());
        dto.setStreet(customer.getStreet());
        dto.setCity(customer.getCity());
        dto.setPostalCode(customer.getPostalCode());
        dto.setDrivingLicenseNo(
                customer.getDrivingLicenseNo()
        );

        dto.setVerificationStatus(
                customer.getVerificationStatus() != null
                        ? customer.getVerificationStatus().name()
                        : "VERIFIED"
        );

        model.addAttribute(
                "customer",
                dto
        );

        model.addAttribute(
                "activeRole",
                session.getAttribute("activeRole")
        );

        return "customers/edit";
    }

    // Updates customer profile and linked User information
    @PostMapping("/customers/update")
    public String updateCustomer(
            @Valid @ModelAttribute("customer") CustomerFormDto formDto,
            BindingResult bindingResult,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        Customer existing =
                customerRepository
                        .findById(formDto.getId())
                        .orElse(null);

        if (existing == null) {
            return "redirect:/customers?error=Customer+not+found";
        }

        // Check for duplicate email if the email has been changed
        String newEmail =
                formDto.getEmail().trim();

        if (!newEmail.equalsIgnoreCase(existing.getEmail())
                && userRepository.existsByEmail(newEmail)) {

            bindingResult.rejectValue(
                    "email",
                    "error.email",
                    "This email address is already in use by another user."
            );
        }

        // Return to the edit form if validation fails
        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "activeRole",
                    session.getAttribute("activeRole")
            );

            return "customers/edit";
        }

        // Update customer profile details
        existing.setFullName(
                formDto.getFullName().trim()
        );

        existing.setEmail(newEmail);

        existing.setPhoneNumber(
                formDto.getPhone().trim()
        );

        existing.setPhone(
                formDto.getPhone().trim()
        );

        existing.setStreet(
                formDto.getStreet().trim()
        );

        existing.setCity(
                formDto.getCity().trim()
        );

        existing.setPostalCode(
                formDto.getPostalCode().trim()
        );

        existing.setDrivingLicenseNo(
                formDto.getDrivingLicenseNo()
        );

        // Update verification status when a valid value is provided
        if (formDto.getVerificationStatus() != null) {

            existing.setVerificationStatus(
                    Customer.VerificationStatus.valueOf(
                            formDto.getVerificationStatus()
                    )
            );
        }

        // Keep the linked User information synchronized
        if (existing.getUser() != null) {

            existing.getUser()
                    .setEmail(newEmail);

            existing.getUser()
                    .setFullName(
                            formDto.getFullName().trim()
                    );

            userRepository.save(
                    existing.getUser()
            );
        }

        customerRepository.save(existing);

        // Record the update action in the audit log
        String activeRole =
                (String) session.getAttribute("activeRole");

        auditService.logAction(
                "staff_user",
                activeRole,
                "UPDATE_CUSTOMER",
                "Customer Management",
                "Updated profile details for: "
                        + existing.getFullName()
                        + " (NIC: "
                        + existing.getNicPassport()
                        + ")"
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Customer profile updated successfully!"
        );

        return "redirect:/customers?success=Customer+updated+successfully";
    }

    // Updates the customer's verification status
    @PostMapping("/customers/verify/{id}")
    public String verifyCustomer(
            @PathVariable("id") Long id,
            @RequestParam("status") String statusStr,
            HttpSession session
    ) {

        Customer customer =
                customerRepository
                        .findById(id)
                        .orElse(null);

        if (customer != null) {

            customer.setVerificationStatus(
                    Customer.VerificationStatus.valueOf(
                            statusStr
                    )
            );

            customerRepository.save(customer);

            // Record the verification action in the audit log
            String activeRole =
                    (String) session.getAttribute("activeRole");

            auditService.logAction(
                    "cro_officer",
                    activeRole,
                    "VERIFY_CUSTOMER_DOCS",
                    "Customer Management",
                    "Verification status set to "
                            + statusStr
                            + " for "
                            + customer.getFullName()
            );
        }

        return "redirect:/customers?success="
                + "Customer+verification+status+updated";
    }

    // Deletes a customer or deactivates the account when linked records exist
    @PostMapping("/customers/delete/{id}")
    public String deleteCustomer(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        try {

            Customer customer =
                    customerRepository
                            .findById(id)
                            .orElse(null);

            if (customer != null) {

                // If the customer has linked vehicles, deactivate the account instead of deleting historical vehicle and policy records
                if (customer.getVehicles() != null
                        && !customer.getVehicles().isEmpty()) {

                    if (customer.getUser() != null) {

                        customer.getUser()
                                .setAccountStatus("Inactive");

                        userRepository.save(
                                customer.getUser()
                        );
                    }

                    redirectAttributes.addFlashAttribute(
                            "successMessage",
                            "Customer account deactivated "
                                    + "(preserved historical vehicle/policy records)."
                    );

                } else {

                    // Delete the Customer record when no linked vehicles exist
                    customerRepository.delete(customer);

                    // Delete the linked User account when available
                    if (customer.getUser() != null) {
                        userRepository.delete(
                                customer.getUser()
                        );
                    }

                    redirectAttributes.addFlashAttribute(
                            "successMessage",
                            "Customer record deleted successfully."
                    );
                }

                // Record the delete or deactivate action
                String activeRole =
                        (String) session.getAttribute("activeRole");

                auditService.logAction(
                        "admin",
                        activeRole,
                        "DELETE_CUSTOMER",
                        "Customer Management",
                        "Deactivated / Deleted customer record: "
                                + customer.getFullName()
                                + " (NIC: "
                                + customer.getNicPassport()
                                + ")"
                );
            }

            return "redirect:/customers?success="
                    + "Customer+action+completed";

        } catch (Exception e) {

            // Prevent deletion when active operational links exist
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Cannot delete customer record "
                            + "with active operational links."
            );

            return "redirect:/customers?error="
                    + "Cannot+delete+customer+with+associated+records";
        }
    }
}