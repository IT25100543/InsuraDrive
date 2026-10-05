package com.insuradrive.controller;

import com.insuradrive.dto.ClaimFormDto;
import com.insuradrive.model.Claim;
import com.insuradrive.model.InsurancePolicy;
import com.insuradrive.repository.ClaimRepository;
import com.insuradrive.repository.InsurancePolicyRepository;
import com.insuradrive.service.AuditService;
import com.insuradrive.service.state.claim.ClaimWorkflowService;
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
 * Controller for Claims Management Module (UC-17: Submit / Process Insurance Claim).
 * Demonstrates:
 * 1. Complete CRUD (Create, Read, Update, Delete/Cancel)
 * 2. Jakarta Bean Validation via ClaimFormDto
 * 3. State Pattern via ClaimWorkflowService
 * 4. SQL Server persistence to table CLAIM
 */
@Controller
public class ClaimController {

    private final ClaimRepository claimRepository;
    private final InsurancePolicyRepository policyRepository;
    private final ClaimWorkflowService claimWorkflowService;
    private final AuditService auditService;

    public ClaimController(ClaimRepository claimRepository,
                           InsurancePolicyRepository policyRepository,
                           ClaimWorkflowService claimWorkflowService,
                           AuditService auditService) {
        this.claimRepository = claimRepository;
        this.policyRepository = policyRepository;
        this.claimWorkflowService = claimWorkflowService;
        this.auditService = auditService;
    }

    @GetMapping("/claims")
    public String listClaims(@RequestParam(value = "status", required = false) String statusStr,
                             Model model, HttpSession session) {
        List<Claim> claims;
        if (statusStr != null && !statusStr.isEmpty()) {
            claims = claimRepository.findByStatus(Claim.ClaimStatus.valueOf(statusStr));
        } else {
            claims = claimRepository.findAll();
        }
        model.addAttribute("claims", claims);
        model.addAttribute("selectedStatus", statusStr);
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "claims/list";
    }

    @GetMapping("/claims/new")
    public String newClaimForm(Model model, HttpSession session) {
        ClaimFormDto formDto = new ClaimFormDto();
        model.addAttribute("claimFormDto", formDto);
        model.addAttribute("claim", new Claim());
        model.addAttribute("policies", policyRepository.findByStatus(InsurancePolicy.PolicyStatus.ACTIVE));
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "claims/form";
    }

    @PostMapping("/claims/save")
    public String saveClaim(@Valid @ModelAttribute("claimFormDto") ClaimFormDto formDto,
                            BindingResult bindingResult,
                            Model model,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("policies", policyRepository.findByStatus(InsurancePolicy.PolicyStatus.ACTIVE));
            model.addAttribute("activeRole", session.getAttribute("activeRole"));
            return "claims/form";
        }

        InsurancePolicy policy = formDto.getPolicyId() != null ? policyRepository.findById(formDto.getPolicyId()).orElse(null) : null;
        if (policy == null || policy.getStatus() != InsurancePolicy.PolicyStatus.ACTIVE) {
            bindingResult.rejectValue("policyId", "error.policy", "Claim can only be lodged against an ACTIVE insurance policy");
            model.addAttribute("policies", policyRepository.findByStatus(InsurancePolicy.PolicyStatus.ACTIVE));
            model.addAttribute("activeRole", session.getAttribute("activeRole"));
            return "claims/form";
        }

        Claim claim = new Claim();
        claim.setPolicy(policy);
        claim.setIncidentDate(formDto.getIncidentDate());
        claim.setSubmissionDate(LocalDate.now());
        claim.setIncidentLocation(formDto.getIncidentLocation());
        claim.setEstimatedDamage(BigDecimal.valueOf(formDto.getClaimedAmount()));
        claim.setDescription(formDto.getDescription());
        claim.setDocumentName(formDto.getDocumentAttachment() != null && !formDto.getDocumentAttachment().trim().isEmpty()
                ? formDto.getDocumentAttachment().trim() : "police_report_verified.pdf");
        claim.setStatus(Claim.ClaimStatus.SUBMITTED);
        claim.setSubmittedAt(LocalDateTime.now());
        claim.setClaimNumber("CLM-2026-" + (int) (Math.random() * 9000 + 1000));

        Claim saved = claimRepository.save(claim);

        String activeRole = (String) session.getAttribute("activeRole");
        auditService.logAction("user", activeRole, "SUBMIT_CLAIM", "Claims Management",
                "Lodged claim: " + saved.getClaimNumber() + " for Amount: LKR " + String.format("%,.2f", saved.getClaimedAmount()) + " on Policy: " + policy.getPolicyNumber());

        redirectAttributes.addFlashAttribute("successMessage", "Claim " + saved.getClaimNumber() + " successfully registered in SQL Server.");
        return "redirect:/claims";
    }

    @GetMapping("/claims/edit/{id}")
    public String editClaimForm(@PathVariable("id") Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Claim claim = claimRepository.findById(id).orElse(null);
        if (claim == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Claim not found");
            return "redirect:/claims";
        }

        ClaimFormDto formDto = new ClaimFormDto();
        formDto.setId(claim.getId());
        formDto.setPolicyId(claim.getPolicy().getId());
        formDto.setIncidentDate(claim.getIncidentDate());
        formDto.setIncidentLocation(claim.getIncidentLocation());
        formDto.setClaimedAmount(claim.getClaimedAmount());
        formDto.setDescription(claim.getDescription());
        formDto.setDocumentAttachment(claim.getDocumentName());

        model.addAttribute("claimFormDto", formDto);
        model.addAttribute("claim", claim);
        model.addAttribute("policies", policyRepository.findByStatus(InsurancePolicy.PolicyStatus.ACTIVE));
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        model.addAttribute("canEdit", claimWorkflowService.canEdit(claim));
        return "claims/edit";
    }

    @PostMapping("/claims/update/{id}")
    public String updateClaim(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("claimFormDto") ClaimFormDto formDto,
                              BindingResult bindingResult,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Claim claim = claimRepository.findById(id).orElse(null);
        if (claim == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Claim not found");
            return "redirect:/claims";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("claim", claim);
            model.addAttribute("policies", policyRepository.findByStatus(InsurancePolicy.PolicyStatus.ACTIVE));
            model.addAttribute("activeRole", session.getAttribute("activeRole"));
            model.addAttribute("canEdit", claimWorkflowService.canEdit(claim));
            return "claims/edit";
        }

        claim.setIncidentDate(formDto.getIncidentDate());
        claim.setIncidentLocation(formDto.getIncidentLocation());
        claim.setEstimatedDamage(BigDecimal.valueOf(formDto.getClaimedAmount()));
        claim.setDescription(formDto.getDescription());
        if (formDto.getDocumentAttachment() != null && !formDto.getDocumentAttachment().trim().isEmpty()) {
            claim.setDocumentName(formDto.getDocumentAttachment().trim());
        }

        claimRepository.save(claim);

        String activeRole = (String) session.getAttribute("activeRole");
        auditService.logAction("user", activeRole, "UPDATE_CLAIM", "Claims Management",
                "Updated details for claim: " + claim.getClaimNumber());

        redirectAttributes.addFlashAttribute("successMessage", "Claim " + claim.getClaimNumber() + " updated successfully.");
        return "redirect:/claims";
    }

    @PostMapping("/claims/update-status")
    public String updateStatus(@RequestParam("claimId") Long claimId,
                               @RequestParam("status") String statusStr,
                               @RequestParam(value = "remarks", required = false) String remarks,
                               @RequestParam(value = "approvedAmount", required = false) Double approvedAmount,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Claim claim = claimRepository.findById(claimId).orElse(null);
        if (claim != null) {
            Claim.ClaimStatus newStatus = Claim.ClaimStatus.valueOf(statusStr);
            // State pattern transition
            claimWorkflowService.transition(claim, newStatus, approvedAmount, remarks);
            claimRepository.save(claim);

            String activeRole = (String) session.getAttribute("activeRole");
            auditService.logAction("claims_officer", activeRole, "UPDATE_CLAIM_STATUS", "Claims Management",
                    "Transitioned claim " + claim.getClaimNumber() + " to state " + newStatus.name() + " (Remarks: " + remarks + ")");
            redirectAttributes.addFlashAttribute("successMessage", "Claim " + claim.getClaimNumber() + " transitioned to " + newStatus.name());
        }
        return "redirect:/claims";
    }

    @PostMapping("/claims/delete/{id}")
    public String deleteClaim(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Claim claim = claimRepository.findById(id).orElse(null);
        if (claim != null) {
            String claimNo = claim.getClaimNumber();
            claimRepository.delete(claim);
            String activeRole = (String) session.getAttribute("activeRole");
            auditService.logAction("manager", activeRole, "DELETE_CLAIM", "Claims Management",
                    "Deleted claim record: " + claimNo);
            redirectAttributes.addFlashAttribute("successMessage", "Claim " + claimNo + " deleted from SQL Server.");
        }
        return "redirect:/claims";
    }

    @GetMapping("/claims/track/{id}")
    public String trackClaim(@PathVariable("id") Long id, Model model, HttpSession session) {
        Claim claim = claimRepository.findById(id).orElse(null);
        if (claim == null) {
            return "redirect:/claims?error=Claim+not+found";
        }
        model.addAttribute("claim", claim);
        model.addAttribute("stateDescription", claimWorkflowService.getStatusDescription(claim));
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "claims/track";
    }
}
