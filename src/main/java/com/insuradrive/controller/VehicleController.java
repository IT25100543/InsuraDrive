package com.insuradrive.controller;

import com.insuradrive.dto.VehicleFormDto;
import com.insuradrive.model.Customer;
import com.insuradrive.model.Vehicle;
import com.insuradrive.repository.CustomerRepository;
import com.insuradrive.repository.VehicleRepository;
import com.insuradrive.service.AuditService;
import com.insuradrive.service.state.vehicle.VehicleStateManager;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for Vehicle Management Module (UC-06: Register Vehicle).
 * Demonstrates:
 * - Full CRUD (Register, View/Search, Update, Delete/Deactivate)
 * - Jakarta Bean Validation (@Valid, BindingResult)
 * - Vehicle State Pattern (VehicleStateManager / VehicleInspectionState)
 * - Relational persistence into SQL Server table VEHICLE.
 */
@Controller
public class VehicleController {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final VehicleStateManager vehicleStateManager;
    private final AuditService auditService;

    public VehicleController(VehicleRepository vehicleRepository,
                             CustomerRepository customerRepository,
                             VehicleStateManager vehicleStateManager,
                             AuditService auditService) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
        this.vehicleStateManager = vehicleStateManager;
        this.auditService = auditService;
    }

    @GetMapping("/vehicles")
    public String listVehicles(@RequestParam(value = "filter", required = false) String filter,
                               @RequestParam(value = "search", required = false) String search,
                               Model model, HttpSession session) {
        List<Vehicle> vehicles;
        if ("inspection".equalsIgnoreCase(filter)) {
            vehicles = vehicleRepository.findByInspectionStatus(Vehicle.InspectionStatus.PENDING_INSPECTION);
        } else if (search != null && !search.trim().isEmpty()) {
            vehicles = vehicleRepository.findAll().stream()
                    .filter(v -> (v.getRegistrationNo() != null && v.getRegistrationNo().toLowerCase().contains(search.trim().toLowerCase()))
                            || (v.getModel() != null && v.getModel().toLowerCase().contains(search.trim().toLowerCase()))
                            || (v.getCustomer() != null && v.getCustomer().getFullName().toLowerCase().contains(search.trim().toLowerCase())))
                    .toList();
        } else {
            vehicles = vehicleRepository.findAll();
        }
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("searchQuery", search);
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "vehicles/list";
    }

    @GetMapping("/vehicles/new")
    public String newVehicleForm(Model model, HttpSession session) {
        if (!model.containsAttribute("vehicle")) {
            model.addAttribute("vehicle", new VehicleFormDto());
        }
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "vehicles/form";
    }

    @PostMapping("/vehicles/save")
    public String saveVehicle(@Valid @ModelAttribute("vehicle") VehicleFormDto formDto,
                              BindingResult bindingResult,
                              Model model, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String reg = formDto.getRegistrationNo() != null ? formDto.getRegistrationNo().trim().toUpperCase() : "";
        String chassis = formDto.getChassisNo() != null ? formDto.getChassisNo().trim().toUpperCase() : "";

        // Uniqueness checks
        if (vehicleRepository.existsByRegistrationNo(reg)) {
            bindingResult.rejectValue("registrationNo", "error.reg", "Vehicle with registration number '" + reg + "' is already enrolled.");
        }
        if (vehicleRepository.findAll().stream().anyMatch(v -> chassis.equalsIgnoreCase(v.getChassisNo()))) {
            bindingResult.rejectValue("chassisNo", "error.chassis", "Vehicle with chassis number '" + chassis + "' is already enrolled.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", customerRepository.findAll());
            model.addAttribute("activeRole", session.getAttribute("activeRole"));
            return "vehicles/form";
        }

        Customer customer = customerRepository.findById(formDto.getCustomerId()).orElse(null);
        if (customer == null) {
            bindingResult.rejectValue("customerId", "error.customer", "Selected customer could not be found.");
            model.addAttribute("customers", customerRepository.findAll());
            model.addAttribute("activeRole", session.getAttribute("activeRole"));
            return "vehicles/form";
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setCustomer(customer);
        vehicle.setRegistrationNo(reg);
        vehicle.setModel(formDto.getModel().trim());
        vehicle.setManufacturerYear(formDto.getManufacturerYear());
        vehicle.setEngineCapacity(formDto.getEngineCapacity());
        vehicle.setChassisNo(chassis);
        vehicle.setEstimatedValue(formDto.getEstimatedValue() != null ? formDto.getEstimatedValue() : 5000000.0);
        vehicle.setVehicleCategory(formDto.getVehicleCategory() != null ? formDto.getVehicleCategory() : "Sedan / Standard");

        // Vehicle State Pattern: evaluates initial inspection state based on vehicle age
        vehicleStateManager.evaluateInitialInspectionState(vehicle);

        Vehicle saved = vehicleRepository.save(vehicle);

        String activeRole = (String) session.getAttribute("activeRole");
        auditService.logAction("cro_user", activeRole, "ENROLL_VEHICLE", "Vehicle Management",
                "Enrolled vehicle via State Pattern: " + saved.getRegistrationNo() + " (" + saved.getModel() + ") for Customer " + customer.getFullName());

        redirectAttributes.addFlashAttribute("successMessage", "Vehicle " + saved.getRegistrationNo() + " enrolled successfully!");
        return "redirect:/vehicles?success=Vehicle+enrolled+successfully";
    }

    @GetMapping("/vehicles/edit/{id}")
    public String editVehicleForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        Vehicle vehicle = vehicleRepository.findById(id).orElse(null);
        if (vehicle == null) {
            return "redirect:/vehicles?error=Vehicle+not+found";
        }

        VehicleFormDto dto = new VehicleFormDto();
        dto.setId(vehicle.getVehicleID());
        dto.setCustomerId(vehicle.getCustomer() != null ? vehicle.getCustomer().getUserID() : null);
        dto.setRegistrationNo(vehicle.getRegistrationNo());
        dto.setModel(vehicle.getModel());
        dto.setManufacturerYear(vehicle.getManufacturerYear());
        dto.setEngineCapacity(vehicle.getEngineCapacity());
        dto.setChassisNo(vehicle.getChassisNo());
        dto.setEstimatedValue(vehicle.getEstimatedValue());
        dto.setVehicleCategory(vehicle.getVehicleCategory());
        dto.setVehicleStatus(vehicle.getVehicleStatus());

        model.addAttribute("vehicle", dto);
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("activeRole", session.getAttribute("activeRole"));
        return "vehicles/edit";
    }

    @PostMapping("/vehicles/update")
    public String updateVehicle(@Valid @ModelAttribute("vehicle") VehicleFormDto formDto,
                                BindingResult bindingResult,
                                Model model, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Vehicle existing = vehicleRepository.findById(formDto.getId()).orElse(null);
        if (existing == null) {
            return "redirect:/vehicles?error=Vehicle+not+found";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", customerRepository.findAll());
            model.addAttribute("activeRole", session.getAttribute("activeRole"));
            return "vehicles/edit";
        }

        existing.setModel(formDto.getModel().trim());
        existing.setManufacturerYear(formDto.getManufacturerYear());
        existing.setEngineCapacity(formDto.getEngineCapacity());
        existing.setChassisNo(formDto.getChassisNo().trim().toUpperCase());
        existing.setEstimatedValue(formDto.getEstimatedValue());
        existing.setVehicleCategory(formDto.getVehicleCategory());
        if (formDto.getCustomerId() != null) {
            customerRepository.findById(formDto.getCustomerId()).ifPresent(existing::setCustomer);
        }

        vehicleRepository.save(existing);

        String activeRole = (String) session.getAttribute("activeRole");
        auditService.logAction("staff_user", activeRole, "UPDATE_VEHICLE", "Vehicle Management",
                "Updated vehicle specifications for: " + existing.getRegistrationNo());

        redirectAttributes.addFlashAttribute("successMessage", "Vehicle details updated successfully!");
        return "redirect:/vehicles?success=Vehicle+details+updated+successfully";
    }

    @PostMapping("/vehicles/inspect/{id}")
    public String recordInspection(@PathVariable("id") Long id,
                                   @RequestParam("status") String statusStr,
                                   @RequestParam(value = "notes", required = false) String notes,
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        Vehicle vehicle = vehicleRepository.findById(id).orElse(null);
        if (vehicle != null) {
            Vehicle.InspectionStatus newStatus = Vehicle.InspectionStatus.valueOf(statusStr);
            // State Pattern: transition inspection state
            vehicleStateManager.transitionInspection(vehicle, newStatus, notes);
            vehicleRepository.save(vehicle);

            String activeRole = (String) session.getAttribute("activeRole");
            auditService.logAction("cro_user", activeRole, "INSPECT_VEHICLE", "Vehicle Management",
                    "Inspection result for " + vehicle.getRegistrationNo() + ": " + statusStr + " (Notes: " + notes + ")");
            redirectAttributes.addFlashAttribute("successMessage", "Inspection status updated to " + statusStr);
        }
        return "redirect:/vehicles?success=Vehicle+inspection+status+updated";
    }

    @PostMapping("/vehicles/delete/{id}")
    public String deleteVehicle(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Vehicle vehicle = vehicleRepository.findById(id).orElse(null);
            if (vehicle != null) {
                // If vehicle has active applications, mark as Deactivated/Scrapped instead of failing FK
                if (vehicle.getApplications() != null && !vehicle.getApplications().isEmpty()) {
                    vehicle.setVehicleStatus("Deactivated");
                    vehicleRepository.save(vehicle);
                    redirectAttributes.addFlashAttribute("successMessage", "Vehicle status set to Deactivated (preserved linked policy history).");
                } else {
                    vehicleRepository.delete(vehicle);
                    redirectAttributes.addFlashAttribute("successMessage", "Vehicle record deleted successfully.");
                }

                String activeRole = (String) session.getAttribute("activeRole");
                auditService.logAction("admin", activeRole, "DELETE_VEHICLE", "Vehicle Management",
                        "Deactivated / Deleted vehicle record: " + vehicle.getRegistrationNo());
            }
            return "redirect:/vehicles?success=Vehicle+action+completed";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete vehicle record with active historical links.");
            return "redirect:/vehicles?error=Cannot+delete+vehicle+with+active+records";
        }
    }
}
