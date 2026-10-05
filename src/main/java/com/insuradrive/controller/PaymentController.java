package com.insuradrive.controller;

import com.insuradrive.repository.InsurancePolicyRepository;
import com.insuradrive.dto.PaymentFormDto;
import com.insuradrive.model.Payment;
import com.insuradrive.model.InsurancePolicy;
import com.insuradrive.model.Receipt;
import com.insuradrive.repository.PaymentRepository;
import com.insuradrive.repository.ReceiptRepository;
import com.insuradrive.service.strategy.payment.PaymentProcessingService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for Payment Management Module (UC-22: Make Premium Payment).
 */
@Controller
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final InsurancePolicyRepository policyRepository;
    private final PaymentProcessingService paymentProcessingService;

    public PaymentController(PaymentRepository paymentRepository,
                             ReceiptRepository receiptRepository,
                             InsurancePolicyRepository policyRepository,
                             PaymentProcessingService paymentProcessingService) {
        this.paymentRepository = paymentRepository;
        this.receiptRepository = receiptRepository;
        this.policyRepository = policyRepository;
        this.paymentProcessingService = paymentProcessingService;
    }

    @GetMapping("/payments")
    public String listPayments(@RequestParam(value = "filter", required = false) String filter,
                               Model model, HttpSession session) {
        List<Payment> payments;
        if ("pending".equalsIgnoreCase(filter)) {
            payments = paymentRepository.findByStatus(Payment.PaymentStatus.PENDING);
        } else {
            payments = paymentRepository.findAll();
        }
        model.addAttribute("payments", payments);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "payments/list";
    }

    @GetMapping("/payments/new")
    public String newPaymentForm(@RequestParam(value = "policyId", required = false) Long policyId,
                                 Model model, HttpSession session) {
        PaymentFormDto formDto = new PaymentFormDto();
        if (policyId != null) {
            policyRepository.findById(policyId).ifPresent(p -> {
                formDto.setPolicyId(p.getId());
                formDto.setAmount(p.getAnnualPremium());
            });
        }
        model.addAttribute("paymentFormDto", formDto);
        model.addAttribute("policies", policyRepository.findAll());
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "payments/form";
    }

    @PostMapping("/payments/save")
    public String savePayment(@Valid @ModelAttribute("paymentFormDto") PaymentFormDto formDto,
                              BindingResult bindingResult,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("policies", policyRepository.findAll());
            model.addAttribute("activeRole", session.getAttribute("activeRole"));
            return "payments/form";
        }

        InsurancePolicy policy = formDto.getPolicyId() != null ? policyRepository.findById(formDto.getPolicyId()).orElse(null) : null;
        if (policy == null) {
            bindingResult.rejectValue("policyId", "error.policy", "Selected policy was not found");
            model.addAttribute("policies", policyRepository.findAll());
            model.addAttribute("activeRole", session.getAttribute("activeRole"));
            return "payments/form";
        }

        // Build domain payment object
        Payment payment = new Payment();
        payment.setPolicy(policy);
        payment.setAmount(formDto.getAmount());
        payment.setPaymentMethod(formDto.getPaymentMethod());
        payment.setPaymentDate(LocalDate.now());
        payment.setDueDate(LocalDate.now().plusMonths(1));
        payment.setInstallmentNo(formDto.getInstallmentNo() != null ? formDto.getInstallmentNo() : 1);
        payment.setPaymentStatus("Paid");

        // Execute Payment Strategy Pattern
        payment = paymentProcessingService.executePayment(payment, policy);

        // Save payment to SQL Server PAYMENT table
        Payment savedPayment = paymentRepository.save(payment);

        // Generate and link official Receipt in SQL Server RECEIPT table
        String receiptNumber = "RCP-2026-" + String.format("%04d", savedPayment.getId());
        Receipt receipt = new Receipt(null, savedPayment, receiptNumber, LocalDateTime.now());
        receiptRepository.save(receipt);
        savedPayment.setReceipt(receipt);

        // Activate policy if it was pending payment
        if (policy.getStatus() == InsurancePolicy.PolicyStatus.PENDING_PAYMENT) {
            policy.setStatus(InsurancePolicy.PolicyStatus.ACTIVE);
            policyRepository.save(policy);
        }

        // auditService call commented out until implemented
        // auditService.logAction(...);

        redirectAttributes.addFlashAttribute("successMessage", "Payment of LKR " + String.format("%,.2f", savedPayment.getAmount()) + " successfully settled and registered in SQL Server.");
        return "redirect:/payments/receipt/" + savedPayment.getId();
    }

    @GetMapping("/payments/receipt/{id}")
    public String viewReceipt(@PathVariable("id") Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment transaction not found");
            return "redirect:/payments";
        }
        model.addAttribute("payment", payment);
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "payments/receipt";
    }

    @GetMapping("/payments/edit/{id}")
    public String editPaymentForm(@PathVariable("id") Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment transaction not found");
            return "redirect:/payments";
        }
        PaymentFormDto formDto = new PaymentFormDto();
        formDto.setId(payment.getId());
        formDto.setPolicyId(payment.getPolicy().getId());
        formDto.setAmount(payment.getAmount());
        formDto.setPaymentMethod(payment.getPaymentMethod());
        formDto.setInstallmentNo(payment.getInstallmentNo());

        model.addAttribute("paymentFormDto", formDto);
        model.addAttribute("payment", payment);
        model.addAttribute("policies", policyRepository.findAll());
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "payments/edit";
    }

    @PostMapping("/payments/update/{id}")
    public String updatePayment(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("paymentFormDto") PaymentFormDto formDto,
                                BindingResult bindingResult,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment transaction not found");
            return "redirect:/payments";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("payment", payment);
            model.addAttribute("policies", policyRepository.findAll());
            model.addAttribute("activeRole", session.getAttribute("activeRole"));
            return "payments/edit";
        }

        payment.setAmount(formDto.getAmount());
        payment.setPaymentMethod(formDto.getPaymentMethod());
        payment.setInstallmentNo(formDto.getInstallmentNo());
        paymentRepository.save(payment);

        // auditService call commented out until implemented
        // auditService.logAction(...);

        redirectAttributes.addFlashAttribute("successMessage", "Payment transaction updated successfully.");
        return "redirect:/payments";
    }

    @PostMapping("/payments/verify/{id}")
    public String verifyPayment(@PathVariable("id") Long id,
                                @RequestParam("action") String action,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment != null) {
            if ("APPROVE".equalsIgnoreCase(action)) {
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setSupervisorVerified(true);
            } else {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setSupervisorVerified(false);
            }
            paymentRepository.save(payment);

            // auditService call commented out until implemented
            // auditService.logAction(...);

            redirectAttributes.addFlashAttribute("successMessage", "Payment verification updated to " + payment.getStatus());
        }
        return "redirect:/payments";
    }

    @PostMapping("/payments/refund/{id}")
    public String refundPayment(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment != null) {
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            // auditService call commented out until implemented
            // auditService.logAction(...);

            redirectAttributes.addFlashAttribute("successMessage", "Transaction " + payment.getTransactionId() + " refunded successfully.");
        }
        return "redirect:/payments";
    }

    @PostMapping("/payments/delete/{id}")
    public String deletePayment(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment != null) {
            receiptRepository.findByPaymentId(payment.getId()).ifPresent(receiptRepository::delete);
            paymentRepository.delete(payment);

            // auditService call commented out until implemented
            // auditService.logAction(...);

            redirectAttributes.addFlashAttribute("successMessage", "Payment record deleted from SQL Server.");
        }
        return "redirect:/payments";
    }
}
