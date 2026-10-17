package com.insuradrive.config;

import com.insuradrive.model.*;
import com.insuradrive.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final com.insuradrive.repository.CustomerRepository customerRepository;
    private final com.insuradrive.repository.VehicleRepository vehicleRepository;
    private final com.insuradrive.repository.InsurancePolicyRepository policyRepository;
    private final com.insuradrive.repository.ClaimRepository claimRepository;
    private final com.insuradrive.repository.PaymentRepository paymentRepository;
    private final com.insuradrive.repository.AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository,
                           com.insuradrive.repository.CustomerRepository customerRepository,
                           com.insuradrive.repository.VehicleRepository vehicleRepository,
                           com.insuradrive.repository.InsurancePolicyRepository policyRepository,
                           com.insuradrive.repository.ClaimRepository claimRepository,
                           com.insuradrive.repository.PaymentRepository paymentRepository,
                           com.insuradrive.repository.AuditLogRepository auditLogRepository,
                           PasswordEncoder passwordEncoder,
                           JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.paymentRepository = paymentRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    private void ensureStaff(Long userId, String employeeNo, String role) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM STAFF WHERE userID = ?", Integer.class, userId);
            if (count == null || count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO STAFF (userID, employeeNo, staffStatus, staffRole) VALUES (?, ?, 'Active', ?)",
                        userId, employeeNo, role);
            }
        } catch (Exception e) {
            System.err.println("Note on ensureStaff for userID " + userId + ": " + e.getMessage());
        }
    }

    private void ensureCustomer(Long userId, String nic, String fullName, String phone) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM CUSTOMER WHERE userID = ?", Integer.class, userId);
            if (count == null || count == 0) {
                String[] parts = fullName.split(" ", 2);
                String firstName = parts[0];
                String lastName = parts.length > 1 ? parts[1] : parts[0];
                jdbcTemplate.update(
                        "INSERT INTO CUSTOMER (userID, NIC, phoneNumber, firstName, lastName, street, city, postalCode) VALUES (?, ?, ?, ?, ?, 'No. 45, Galle Road', 'Colombo', '00300')",
                        userId, nic, phone, firstName, lastName);
            }
        } catch (Exception e) {
            System.err.println("Note on ensureCustomer for userID " + userId + ": " + e.getMessage());
        }
    }

    @Override
    public void run(String... args) throws Exception {
        // 0. Ensure any existing plain-text passwords in database are safely migrated to BCrypt hashes
        for (User u : userRepository.findAll()) {
            String pwd = u.getPassword();
            if (pwd != null && !pwd.startsWith("$2a$") && !pwd.startsWith("$2b$") && !pwd.startsWith("$2y$")) {
                u.setPassword(passwordEncoder.encode(pwd));
                userRepository.save(u);
            }
        }

        String encodedPassword = passwordEncoder.encode("password");

        // 1. Branch Manager demo user: manager1 / password
        User managerUser = userRepository.findByUsername("manager1").orElseGet(() -> {
            return userRepository.save(User.builder()
                    .username("manager1")
                    .password(encodedPassword)
                    .fullName("Ranasinghe A. (Branch Manager)")
                    .email("manager@insuradrive.com")
                    .accountStatus("Active")
                    .build());
        });
        ensureStaff(managerUser.getUserID(), "EMP-MGR1", "Branch Operations Manager");

        // 2. Customer Relations Officer demo user: cro1 / password
        User croUser = userRepository.findByUsername("cro1").orElseGet(() -> {
            return userRepository.save(User.builder()
                    .username("cro1")
                    .password(encodedPassword)
                    .fullName("Sharon M (CRO)")
                    .email("cro@insuradrive.com")
                    .accountStatus("Active")
                    .build());
        });
        ensureStaff(croUser.getUserID(), "EMP-CRO1", "Customer Relations Officer");

        // 3. Claims Executive demo user: claims1 / password
        User claimsUser = userRepository.findByUsername("claims1").orElseGet(() -> {
            return userRepository.save(User.builder()
                    .username("claims1")
                    .password(encodedPassword)
                    .fullName("Ahamed M.A. (Claims Officer)")
                    .email("claims@insuradrive.com")
                    .accountStatus("Active")
                    .build());
        });
        ensureStaff(claimsUser.getUserID(), "EMP-CLM1", "Claims Processing Executive");

        // 4. Finance Supervisor demo user: finance1 / password
        User financeUser = userRepository.findByUsername("finance1").orElseGet(() -> {
            return userRepository.save(User.builder()
                    .username("finance1")
                    .password(encodedPassword)
                    .fullName("Jayasinghe M. (Finance Supervisor)")
                    .email("finance@insuradrive.com")
                    .accountStatus("Active")
                    .build());
        });
        ensureStaff(financeUser.getUserID(), "EMP-FIN1", "Finance and Payment Supervisor");

        // 5. Risk & Compliance Officer demo user: risk1 / password
        User complianceUser = userRepository.findByUsername("risk1").orElseGet(() -> {
            return userRepository.save(User.builder()
                    .username("risk1")
                    .password(encodedPassword)
                    .fullName("Grero V. S (Risk & Compliance)")
                    .email("compliance@insuradrive.com")
                    .accountStatus("Active")
                    .build());
        });
        ensureStaff(complianceUser.getUserID(), "EMP-RSK1", "Risk and Compliance Officer");

        // 6. Administrator demo user: admin1 / password
        User adminUser = userRepository.findByUsername("admin1").orElseGet(() -> {
            return userRepository.save(User.builder()
                    .username("admin1")
                    .password(encodedPassword)
                    .fullName("System Administrator")
                    .email("admin@insuradrive.com")
                    .accountStatus("Active")
                    .build());
        });
        ensureStaff(adminUser.getUserID(), "EMP-ADM1", "System Administrator");

        // 7. Customer demo user: customer1 / password
        User customerUser = userRepository.findByUsername("customer1").orElseGet(() -> {
            return userRepository.save(User.builder()
                    .username("customer1")
                    .password(encodedPassword)
                    .fullName("Kamal Perera")
                    .email("kamal.perera@gmail.com")
                    .accountStatus("Active")
                    .build());
        });
        ensureCustomer(customerUser.getUserID(), "199012345678", "Kamal Perera", "0771234567");

        // 8. If policies table is empty, initialize demo policies, claims, and payments
        if (policyRepository.count() == 0) {
            Customer c1 = customerRepository.findByNicPassport("199012345678")
                    .orElseGet(() -> customerRepository.findAll().stream().findFirst().orElse(null));

            Vehicle v1 = vehicleRepository.findAll().stream().findFirst().orElse(null);

            if (c1 != null && v1 != null) {
                InsurancePolicy p1 = policyRepository.save(InsurancePolicy.builder()
                        .policyNumber("POL-2026-001")
                        .policyType(InsurancePolicy.PolicyType.COMPREHENSIVE_PREMIUM)
                        .sumInsured(12500000.0)
                        .annualPremium(187500.0)
                        .startDate(LocalDate.now().minusMonths(3))
                        .endDate(LocalDate.now().plusMonths(9))
                        .status(InsurancePolicy.PolicyStatus.ACTIVE)
                        .renewalCount(0)
                        .customer(c1)
                        .vehicle(v1)
                        .build());

                claimRepository.save(Claim.builder()
                        .claimNumber("CLM-2026-8801")
                        .incidentDate(LocalDate.now().minusDays(10))
                        .incidentLocation("Duplication Road, Colombo 03")
                        .description("Rear bumper minor collision while parking. Required bumper replacement and repainting.")
                        .claimedAmount(145000.0)
                        .approvedAmount(135000.0)
                        .status(Claim.ClaimStatus.UNDER_VERIFICATION)
                        .documentName("police_report_8801.pdf")
                        .officerRemarks("Initial inspection completed by assessor. Awaiting final repair bill.")
                        .policy(p1)
                        .build());

                paymentRepository.save(Payment.builder()
                        .transactionId("TXN-994812")
                        .amount(187500.0)
                        .paymentMethod("Online Credit Card (Visa)")
                        .paymentType(Payment.PaymentType.FULL_PAYMENT)
                        .installmentNo(1)
                        .supervisorVerified(true)
                        .status(Payment.PaymentStatus.COMPLETED)
                        .receiptNumber("RCP-2026-994812")
                        .policy(p1)
                        .paymentDate(LocalDateTime.now().minusMonths(3))
                        .build());
            }
        }

        // 9. If audit log is empty, seed initial logs
        if (auditLogRepository.count() == 0) {
            auditLogRepository.save(AuditLog.builder()
                    .timestamp(LocalDateTime.now().minusHours(5))
                    .username("system")
                    .userRole("SYSTEM")
                    .action("DATABASE_BOOTSTRAP")
                    .targetModule("SYSTEM")
                    .details("InsuraDrive system initialized with default seeds and standard policies.")
                    .build());
        }

        System.out.println(">>> InsuraDrive Demo Users & Data Verification Completed Successfully! <<<");
    }
}
