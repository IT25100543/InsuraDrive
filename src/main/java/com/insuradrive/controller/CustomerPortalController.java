package com.insuradrive.controller;

import com.insuradrive.model.*;
import com.insuradrive.repository.*;
import com.insuradrive.service.AuditService;
import com.insuradrive.service.CustomerSecurityService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerPortalController {

    private final CustomerSecurityService customerSecurityService;
    private final com.insuradrive.repository.CustomerRepository customerRepository;
    private final com.insuradrive.repository.VehicleRepository vehicleRepository;
    private final com.insuradrive.repository.InsurancePolicyRepository policyRepository;
    private final com.insuradrive.repository.ClaimRepository claimRepository;
    private final com.insuradrive.repository.PaymentRepository paymentRepository;
    private final com.insuradrive.repository.InsurancePackageRepository packageRepository;
    private final com.insuradrive.repository.PolicyApplicationRepository applicationRepository;
    private final com.insuradrive.repository.CustomerDocumentRepository documentRepository;
    private final com.insuradrive.repository.ReceiptRepository receiptRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public CustomerPortalController(CustomerSecurityService customerSecurityService,
                                    com.insuradrive.repository.CustomerRepository customerRepository,
                                    com.insuradrive.repository.VehicleRepository vehicleRepository,
                                    com.insuradrive.repository.InsurancePolicyRepository policyRepository,
                                    com.insuradrive.repository.ClaimRepository claimRepository,
                                    com.insuradrive.repository.PaymentRepository paymentRepository,
                                    com.insuradrive.repository.InsurancePackageRepository packageRepository,
                                    com.insuradrive.repository.PolicyApplicationRepository applicationRepository,
                                    com.insuradrive.repository.CustomerDocumentRepository documentRepository,
                                    com.insuradrive.repository.ReceiptRepository receiptRepository,
                                    UserRepository userRepository,
                                    AuditService auditService) {
        this.customerSecurityService = customerSecurityService;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.paymentRepository = paymentRepository;
        this.packageRepository = packageRepository;
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.receiptRepository = receiptRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    private void prepareModel(Model model, Customer customer, HttpSession session) {
        session.setAttribute("activeRole", "CUSTOMER");
        session.setAttribute("displayName", customer.getFullName());
        model.addAttribute("activeRole", "CUSTOMER");
        model.addAttribute("customer", customer);
    }

    // 1. Customer Dashboard
    @GetMapping("/dashboard")
    public String customerDashboard(Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        prepareModel(model, customer, session);

        Long cid = customer.getUserID();
        List<InsurancePolicy> myPolicies = policyRepository.findByCustomerId(cid);
        List<Claim> myClaims = claimRepository.findByCustomerId(cid);
        List<Vehicle> myVehicles = vehicleRepository.findByCustomerId(cid);
        List<Payment> myPayments = paymentRepository.findByCustomerId(cid);
        List<PolicyApplication> myApps = applicationRepository.findByCustomerId(cid);

        model.addAttribute("myPolicies", myPolicies);
        model.addAttribute("myClaims", myClaims);
        model.addAttribute("myVehicles", myVehicles);
        model.addAttribute("myPayments", myPayments);
        model.addAttribute("myApplications", myApps);

        model.addAttribute("totalVehicles", myVehicles.size());
        model.addAttribute("totalPolicies", myPolicies.size());
        model.addAttribute("totalClaims", myClaims.size());
        model.addAttribute("totalPayments", myPayments.size());

        return "customer/dashboard";
    }

    // 2. Profile
    @GetMapping("/profile")
    public String viewProfile(Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        prepareModel(model, customer, session);
        return "customer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication auth,
                                @RequestParam(value = "firstName", required = false) String firstName,
                                @RequestParam(value = "middleName", required = false) String middleName,
                                @RequestParam(value = "lastName", required = false) String lastName,
                                @RequestParam("phoneNumber") String phoneNumber,
                                @RequestParam("street") String street,
                                @RequestParam("city") String city,
                                @RequestParam("postalCode") String postalCode,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        if (firstName != null && !firstName.trim().isEmpty()) {
            customer.setFirstName(firstName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            customer.setLastName(lastName.trim());
        }
        customer.setPhoneNumber(phoneNumber.trim());
        customer.setPhone(phoneNumber.trim());
        customer.setMiddleName(middleName != null && !middleName.trim().isEmpty() ? middleName.trim() : null);
        customer.setStreet(street.trim());
        customer.setCity(city.trim());
        customer.setPostalCode(postalCode.trim());
        customer.setFullName(null); // Recalculate full name dynamically from firstName, middleName, and lastName

        Customer savedCustomer = customerRepository.save(customer);
        if (savedCustomer.getUser() != null) {
            savedCustomer.getUser().setFullName(savedCustomer.getFullName());
            userRepository.save(savedCustomer.getUser());
        }
        if (session != null) {
            session.setAttribute("displayName", savedCustomer.getFullName());
            session.setAttribute("currentUser", savedCustomer.getFullName());
        }
        auditService.logAction(auth.getName(), "CUSTOMER", "UPDATE_PROFILE", "Customer Portal",
                "Updated personal profile details");
        redirectAttributes.addFlashAttribute("successMessage", "Your profile details have been updated successfully.");
        return "redirect:/customer/profile";
    }

    // 3. Vehicles
    @GetMapping("/vehicles")
    public String myVehicles(Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        prepareModel(model, customer, session);
        model.addAttribute("vehicles", vehicleRepository.findByCustomerId(customer.getUserID()));
        return "customer/vehicles";
    }

    @GetMapping("/vehicles/new")
    public String newVehicleForm(Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        prepareModel(model, customer, session);
        model.addAttribute("vehicle", new Vehicle());
        return "customer/vehicle-form";
    }

    @PostMapping("/vehicles/save")
    public String saveVehicle(Authentication auth,
                              @ModelAttribute Vehicle vehicle,
                              RedirectAttributes redirectAttributes) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);

        if (vehicle.getRegistrationNo() == null || vehicle.getRegistrationNo().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vehicle registration number is required.");
            return "redirect:/customer/vehicles/new";
        }

        String reg = vehicle.getRegistrationNo().trim().toUpperCase();
        if (vehicleRepository.existsByRegistrationNo(reg)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vehicle with registration " + reg + " already exists in the system.");
            return "redirect:/customer/vehicles/new";
        }

        vehicle.setRegistrationNo(reg);
        vehicle.setCustomer(customer);
        vehicle.setVehicleStatus("Active");
        if (vehicle.getManufacturerYear() == null) {
            vehicle.setManufacturerYear(LocalDate.now().getYear());
        }
        if (vehicle.getEngineCapacity() == null) {
            vehicle.setEngineCapacity(1500);
        }

        Vehicle saved = vehicleRepository.save(vehicle);
        auditService.logAction(auth.getName(), "CUSTOMER", "ENROLL_VEHICLE", "Vehicle Enrollment",
                "Enrolled vehicle: " + saved.getRegistrationNo() + " (" + saved.getMakeModel() + ")");
        redirectAttributes.addFlashAttribute("successMessage", "Vehicle " + saved.getRegistrationNo() + " enrolled successfully!");
        return "redirect:/customer/vehicles";
    }

    // 4. Insurance Packages
    @GetMapping("/packages")
    public String viewPackages(Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        prepareModel(model, customer, session);
        model.addAttribute("packages", packageRepository.findAll());
        return "customer/packages";
    }

    // 5. Apply for Insurance
    @GetMapping("/apply")
    public String applyForPolicyForm(@RequestParam(value = "packageId", required = false) Integer packageId,
                                     @RequestParam(value = "vehicleId", required = false) Long vehicleId,
                                     Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        prepareModel(model, customer, session);

        model.addAttribute("vehicles", vehicleRepository.findByCustomerId(customer.getUserID()));
        model.addAttribute("packages", packageRepository.findAll());
        model.addAttribute("selectedPackageId", packageId);
        model.addAttribute("selectedVehicleId", vehicleId);
        return "customer/apply";
    }

    @PostMapping("/apply")
    public String submitPolicyApplication(Authentication auth,
                                          @RequestParam("vehicleId") Long vehicleId,
                                          @RequestParam("packageId") Integer packageId,
                                          RedirectAttributes redirectAttributes) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        Vehicle vehicle = customerSecurityService.assertVehicleOwnership(customer, vehicleId);

        InsurancePackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Insurance package not found"));

        PolicyApplication application = new PolicyApplication();
        application.setCustomer(customer);
        application.setVehicle(vehicle);
        application.setInsurancePackage(pkg);
        application.setCalculatedPremium(pkg.getBasePremium());
        application.setApplicationDate(LocalDate.now());
        application.setStatus("Pending Review");

        PolicyApplication saved = applicationRepository.save(application);
        auditService.logAction(auth.getName(), "CUSTOMER", "APPLY_POLICY", "Policy Application",
                "Submitted policy application #" + saved.getApplicationID() + " for vehicle " + vehicle.getRegistrationNo());

        redirectAttributes.addFlashAttribute("successMessage",
                "Application submitted successfully for " + vehicle.getRegistrationNo() + "! Our underwriters are reviewing it.");
        return "redirect:/customer/applications";
    }

    // 6. My Applications
    @GetMapping("/applications")
    public String myApplications(Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        prepareModel(model, customer, session);
        model.addAttribute("applications", applicationRepository.findByCustomerId(customer.getUserID()));
        return "customer/applications";
    }

    // 7. My Policies
    @GetMapping("/policies")
    public String myPolicies(Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        prepareModel(model, customer, session);
        model.addAttribute("policies", policyRepository.findByCustomerId(customer.getUserID()));
        return "customer/policies";
    }

    @GetMapping("/policies/{id}")
    public String viewPolicyDetail(@PathVariable("id") Long id,
                                   Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        InsurancePolicy policy = customerSecurityService.assertPolicyOwnership(customer, id);
        prepareModel(model, customer, session);

        model.addAttribute("policy", policy);
        model.addAttribute("claims", claimRepository.findByPolicyId(policy.getId()));
        model.addAttribute("payments", paymentRepository.findByPolicyId(policy.getId()));
        return "customer/policy-detail";
    }

    // 8. Payments & Receipts
    @GetMapping("/payments")
    public String myPayments(Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        prepareModel(model, customer, session);

        List<Payment> payments = paymentRepository.findByCustomerId(customer.getUserID());
        List<InsurancePolicy> policies = policyRepository.findByCustomerId(customer.getUserID());
        List<InsurancePolicy> pendingPolicies = policies.stream()
                .filter(p -> p.getStatus() == InsurancePolicy.PolicyStatus.PENDING_PAYMENT)
                .toList();

        model.addAttribute("payments", payments);
        model.addAttribute("policies", policies);
        model.addAttribute("pendingPolicies", pendingPolicies);
        return "customer/payments";
    }

    @GetMapping("/payments/pay")
    public String payPremiumForm(@RequestParam(value = "policyId", required = false) Long policyId,
                                 Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        prepareModel(model, customer, session);

        List<InsurancePolicy> policies = policyRepository.findByCustomerId(customer.getUserID());
        model.addAttribute("policies", policies);

        Payment payment = new Payment();
        if (policyId != null) {
            InsurancePolicy policy = customerSecurityService.assertPolicyOwnership(customer, policyId);
            payment.setPolicy(policy);
            payment.setAmount(policy.getAnnualPremium());
        }
        model.addAttribute("payment", payment);
        model.addAttribute("paymentTypes", Payment.PaymentType.values());
        return "customer/payment-form";
    }

    @PostMapping("/payments/pay")
    public String processPayment(Authentication auth,
                                 @RequestParam("policyId") Long policyId,
                                 @RequestParam(value = "paymentMethod", defaultValue = "Credit Card") String paymentMethod,
                                 @RequestParam(value = "paymentType", defaultValue = "FULL_PAYMENT") String paymentTypeStr,
                                 RedirectAttributes redirectAttributes) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        InsurancePolicy policy = customerSecurityService.assertPolicyOwnership(customer, policyId);

        Payment payment = new Payment();
        payment.setPolicy(policy);
        Payment.PaymentType pt = Payment.PaymentType.FULL_PAYMENT;
        try {
            pt = Payment.PaymentType.valueOf(paymentTypeStr);
        } catch (Exception e) {
            pt = Payment.PaymentType.FULL_PAYMENT;
        }
        payment.setPaymentType(pt);

        double baseAmount = policy.getAnnualPremium() != null ? policy.getAnnualPremium() : 45000.0;
        if (pt != Payment.PaymentType.FULL_PAYMENT) {
            payment.setAmount(BigDecimal.valueOf(Math.round((baseAmount / 4.0) * 100.0) / 100.0));
        } else {
            payment.setAmount(BigDecimal.valueOf(baseAmount));
        }
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionId("TXN-" + System.currentTimeMillis() % 1000000);
        payment.setReceiptNumber("RCP-2026-" + (int)(Math.random() * 9000 + 1000));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setSupervisorVerified(true);

        Payment saved = paymentRepository.save(payment);

        String receiptNumber = "RCP-2026-" + String.format("%04d", saved.getId());
        Receipt receipt = new Receipt(null, saved, receiptNumber, LocalDateTime.now());
        receiptRepository.save(receipt);
        saved.setReceipt(receipt);

        if (policy.getStatus() == InsurancePolicy.PolicyStatus.PENDING_PAYMENT) {
            policy.setStatus(InsurancePolicy.PolicyStatus.ACTIVE);
            policyRepository.save(policy);
        }

        auditService.logAction(auth.getName(), "CUSTOMER", "MAKE_PAYMENT", "Payment Settlement",
                "Settled premium payment: " + saved.getTransactionId() + " (LKR " + saved.getAmount() + ") for Policy " + policy.getPolicyNumber());

        redirectAttributes.addFlashAttribute("successMessage", "Payment completed successfully! Receipt generated.");
        return "redirect:/customer/receipts/" + saved.getId();
    }

    @GetMapping("/receipts")
    public String myReceipts(Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        prepareModel(model, customer, session);
        model.addAttribute("payments", paymentRepository.findByCustomerId(customer.getUserID()));
        return "customer/receipts";
    }

    @GetMapping("/receipts/{id}")
    public String viewReceipt(@PathVariable("id") Long id,
                              Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        Payment payment = customerSecurityService.assertPaymentOwnership(customer, id);
        prepareModel(model, customer, session);

        model.addAttribute("payment", payment);
        return "customer/receipt";
    }

    // 9. Claims
    @GetMapping("/claims")
    public String myClaims(Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        prepareModel(model, customer, session);
        model.addAttribute("claims", claimRepository.findByCustomerId(customer.getUserID()));
        return "customer/claims";
    }

    @GetMapping("/claims/new")
    public String newClaimForm(Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        prepareModel(model, customer, session);

        List<InsurancePolicy> myActivePolicies = policyRepository.findByCustomerId(customer.getUserID())
                .stream()
                .filter(p -> p.getStatus() == InsurancePolicy.PolicyStatus.ACTIVE)
                .toList();

        model.addAttribute("claim", new Claim());
        model.addAttribute("policies", myActivePolicies);
        return "customer/claim-form";
    }

    @PostMapping("/claims/save")
    public String submitClaim(Authentication auth,
                              @ModelAttribute Claim claim,
                              @RequestParam("policyId") Long policyId,
                              RedirectAttributes redirectAttributes) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        InsurancePolicy policy = customerSecurityService.assertPolicyOwnership(customer, policyId);

        claim.setPolicy(policy);
        claim.setClaimNumber("CLM-2026-" + (int)(Math.random() * 9000 + 1000));
        claim.setStatus(Claim.ClaimStatus.SUBMITTED);
        claim.setSubmissionDate(LocalDate.now());
        claim.setSubmittedAt(LocalDateTime.now());
        if (claim.getIncidentDate() == null) {
            claim.setIncidentDate(LocalDate.now());
        }
        if (claim.getIncidentLocation() == null || claim.getIncidentLocation().trim().isEmpty()) {
            claim.setIncidentLocation("Colombo");
        }
        if (claim.getEstimatedDamage() == null) {
            claim.setEstimatedDamage(BigDecimal.valueOf(claim.getClaimedAmount() != null && claim.getClaimedAmount() > 0 ? claim.getClaimedAmount() : 50000.0));
        }
        if (claim.getDescription() == null || claim.getDescription().trim().isEmpty()) {
            claim.setDescription("Vehicle sustained damage in traffic incident. Lodged by insured customer.");
        }

        Claim saved = claimRepository.save(claim);
        auditService.logAction(auth.getName(), "CUSTOMER", "SUBMIT_CLAIM", "Claims Filing",
                "Filed insurance claim " + saved.getClaimNumber() + " on Policy " + policy.getPolicyNumber());

        redirectAttributes.addFlashAttribute("successMessage",
                "Claim " + saved.getClaimNumber() + " submitted successfully. A claims assessor will review your case.");
        return "redirect:/customer/claims";
    }

    // 10. Claim Documents & Verification Files
    @GetMapping("/documents")
    public String myDocuments(Authentication auth, Model model, HttpSession session) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);
        prepareModel(model, customer, session);
        model.addAttribute("documents", documentRepository.findByCustomerId(customer.getUserID()));
        return "customer/documents";
    }

    @PostMapping("/documents/upload")
    public String uploadDocument(Authentication auth,
                                 @RequestParam("documentType") String documentType,
                                 @RequestParam("documentNumber") String documentNumber,
                                 RedirectAttributes redirectAttributes) {
        Customer customer = customerSecurityService.getAuthenticatedCustomer(auth);

        CustomerDocument doc = new CustomerDocument();
        doc.setCustomer(customer);
        doc.setDocumentType(documentType.trim());
        doc.setDocumentNumber(documentNumber.trim());
        doc.setVerificationStatus("Pending");
        doc.setUploadDate(LocalDateTime.now());

        documentRepository.save(doc);
        auditService.logAction(auth.getName(), "CUSTOMER", "UPLOAD_DOCUMENT", "Document Verification",
                "Uploaded customer document: " + doc.getDocumentType() + " (" + doc.getDocumentNumber() + ")");

        redirectAttributes.addFlashAttribute("successMessage", "Document recorded successfully for verification.");
        return "redirect:/customer/documents";
    }
}
