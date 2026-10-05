package com.insuradrive.controller;

import com.insuradrive.dto.CustomerRegistrationDto;
import com.insuradrive.service.CustomerRegistrationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Handles customer registration requests
// Displays the registration form, validates submitted customer data, and delegates account creation to CustomerRegistrationService

@Controller
public class RegistrationController {

    private final CustomerRegistrationService registrationService;

    // Injects the customer registration service
    public RegistrationController(CustomerRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    // Displays the customer registration form
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {

        if (!model.containsAttribute("registrationDto")) {
            model.addAttribute(
                    "registrationDto",
                    new CustomerRegistrationDto()
            );
        }

        return "register";
    }

    // Processes the submitted customer registration form
    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("registrationDto") CustomerRegistrationDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        // Return to the form if validation errors are present
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            // Delegate the registration process to the service layer
            registrationService.registerCustomer(dto);

            // Display a success message after redirection
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your InsuraDrive customer account has been created successfully! "
                            + "Please sign in below."
            );

            return "redirect:/login?registered=true";

        } catch (IllegalArgumentException ex) {

            // Display validation or duplicate-account errors
            model.addAttribute(
                    "registrationError",
                    ex.getMessage()
            );

            return "register";

        } catch (Exception ex) {

            // Display a general message for unexpected registration failures
            model.addAttribute(
                    "registrationError",
                    "Registration could not be completed. "
                            + "Please verify your information and try again."
            );

            return "register";
        }
    }
}