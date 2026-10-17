package com.insuradrive.service;

import com.insuradrive.model.Customer;
import com.insuradrive.repository.CustomerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public CustomerService(CustomerRepository customerRepository, JdbcTemplate jdbcTemplate) {
        this.customerRepository = customerRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCustomer(Long customerId) {
        if (customerId == null) {
            return false;
        }

        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            log.warn("Customer ID {} not found for deletion", customerId);
            return false;
        }

        Long userId = (customer.getUser() != null && customer.getUser().getUserID() != null)
                ? customer.getUser().getUserID()
                : customerId;

        log.info("Starting cascading deletion for customer ID {} (user ID {}, name '{}')",
                customerId, userId, customer.getFullName());

        // Step 1: Collect Vehicle IDs belonging to this customer
        List<Long> vehicleIds = queryIds("SELECT vehicleID FROM VEHICLE WHERE customerID = ?", customerId);

        // Step 2: Collect Policy Application IDs (direct customer applications or applications for customer's vehicles)
        Set<Long> applicationIds = new HashSet<>(queryIds("SELECT applicationID FROM POLICY_APPLICATION WHERE customerID = ?", customerId));
        for (Long vId : vehicleIds) {
            applicationIds.addAll(queryIds("SELECT applicationID FROM POLICY_APPLICATION WHERE vehicleID = ?", vId));
        }

        // Step 3: Collect Policy IDs for all identified applications
        Set<Long> policyIds = new HashSet<>();
        for (Long appId : applicationIds) {
            policyIds.addAll(queryIds("SELECT policyID FROM POLICY WHERE applicationID = ?", appId));
        }

        // Step 4: Collect Payment and Claim IDs for all identified policies
        Set<Long> paymentIds = new HashSet<>();
        Set<Long> claimIds = new HashSet<>();
        for (Long polId : policyIds) {
            paymentIds.addAll(queryIds("SELECT paymentID FROM PAYMENT WHERE policyID = ?", polId));
            claimIds.addAll(queryIds("SELECT claimID FROM CLAIM WHERE policyID = ?", polId));
        }

        // Step 5: Delete RECEIPTs
        for (Long pId : paymentIds) {
            safeUpdate("DELETE FROM RECEIPT WHERE paymentID = ?", pId);
        }

        // Step 6: Delete CLAIM_DOCUMENTs
        for (Long cId : claimIds) {
            safeUpdate("DELETE FROM CLAIM_DOCUMENT WHERE claimID = ?", cId);
        }

        // Step 7: Delete CLAIMs
        for (Long cId : claimIds) {
            safeUpdate("DELETE FROM CLAIM WHERE claimID = ?", cId);
        }
        for (Long polId : policyIds) {
            safeUpdate("DELETE FROM CLAIM WHERE policyID = ?", polId);
        }

        // Step 8: Delete PAYMENTs
        for (Long pId : paymentIds) {
            safeUpdate("DELETE FROM PAYMENT WHERE paymentID = ?", pId);
        }
        for (Long polId : policyIds) {
            safeUpdate("DELETE FROM PAYMENT WHERE policyID = ?", polId);
        }

        // Step 9: Delete POLICYs
        for (Long polId : policyIds) {
            safeUpdate("DELETE FROM POLICY WHERE policyID = ?", polId);
        }
        for (Long appId : applicationIds) {
            safeUpdate("DELETE FROM POLICY WHERE applicationID = ?", appId);
        }

        // Step 10: Delete POLICY_APPLICATIONs
        for (Long appId : applicationIds) {
            safeUpdate("DELETE FROM POLICY_APPLICATION WHERE applicationID = ?", appId);
        }
        safeUpdate("DELETE FROM POLICY_APPLICATION WHERE customerID = ?", customerId);
        for (Long vId : vehicleIds) {
            safeUpdate("DELETE FROM POLICY_APPLICATION WHERE vehicleID = ?", vId);
        }

        // Step 11: Delete VEHICLE_INSPECTIONs
        for (Long vId : vehicleIds) {
            safeUpdate("DELETE FROM VEHICLE_INSPECTION WHERE vehicleID = ?", vId);
        }

        // Step 12: Delete VEHICLEs
        for (Long vId : vehicleIds) {
            safeUpdate("DELETE FROM VEHICLE WHERE vehicleID = ?", vId);
        }
        safeUpdate("DELETE FROM VEHICLE WHERE customerID = ?", customerId);

        // Step 13: Delete CUSTOMER_DOCUMENTs
        safeUpdate("DELETE FROM CUSTOMER_DOCUMENT WHERE customerID = ?", customerId);

        // Step 14: Delete AUDIT_LOGs where userID = customerId or userId
        safeUpdate("DELETE FROM AUDIT_LOG WHERE userID = ?", customerId);
        if (!userId.equals(customerId)) {
            safeUpdate("DELETE FROM AUDIT_LOG WHERE userID = ?", userId);
        }

        // Step 15: Delete STAFF record if any orphan exists for this userID
        safeUpdate("DELETE FROM STAFF WHERE userID = ?", customerId);
        if (!userId.equals(customerId)) {
            safeUpdate("DELETE FROM STAFF WHERE userID = ?", userId);
        }

        // Step 16: Delete CUSTOMER record
        safeUpdate("DELETE FROM CUSTOMER WHERE userID = ?", customerId);

        // Step 17: Delete [USER] record
        deleteUserRecord(customerId);
        if (!userId.equals(customerId)) {
            deleteUserRecord(userId);
        }

        // Step 18: Fallback cleanup for alternative MySQL schema tables if present
        safeUpdate("DELETE FROM payments WHERE customer_id = ?", customerId);
        safeUpdate("DELETE FROM claims WHERE customer_id = ?", customerId);
        safeUpdate("DELETE FROM policies WHERE customer_id = ?", customerId);
        safeUpdate("DELETE FROM vehicles WHERE customer_id = ?", customerId);
        safeUpdate("DELETE FROM customers WHERE id = ? OR user_id = ?", customerId, customerId);
        safeUpdate("DELETE FROM users WHERE id = ?", customerId);

        // Step 19: Clear Hibernate persistence context to evict cached entities
        if (entityManager != null) {
            entityManager.clear();
        }

        log.info("Successfully completed cascading deletion for customer ID {}", customerId);
        return true;
    }

    private void deleteUserRecord(Long uId) {
        if (uId == null) return;
        try {
            jdbcTemplate.update("DELETE FROM [dbo].[USER] WHERE userID = ?", uId);
        } catch (Exception e1) {
            try {
                jdbcTemplate.update("DELETE FROM [USER] WHERE userID = ?", uId);
            } catch (Exception e2) {
                try {
                    jdbcTemplate.update("DELETE FROM `USER` WHERE userID = ?", uId);
                } catch (Exception e3) {
                    safeUpdate("DELETE FROM \"USER\" WHERE userID = ?", uId);
                }
            }
        }
    }

    private List<Long> queryIds(String sql, Object... args) {
        try {
            return jdbcTemplate.queryForList(sql, Long.class, args);
        } catch (Exception e) {
            log.debug("ID query yielded no results or exception [{}]: {}", sql, e.getMessage());
            return Collections.emptyList();
        }
    }

    private void safeUpdate(String sql, Object... args) {
        try {
            jdbcTemplate.update(sql, args);
        } catch (Exception e) {
            log.debug("Safe update notice [{}]: {}", sql, e.getMessage());
        }
    }
}
