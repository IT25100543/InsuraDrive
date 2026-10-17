package com.insuradrive.service;

import com.insuradrive.model.*;
import com.insuradrive.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CustomerSecurityService {

    private final com.insuradrive.repository.UserRepository userRepository;
    private final com.insuradrive.repository.CustomerRepository customerRepository;
    private final com.insuradrive.repository.InsurancePolicyRepository policyRepository;
    private final com.insuradrive.repository.VehicleRepository vehicleRepository;
    private final com.insuradrive.repository.ClaimRepository claimRepository;
    private final com.insuradrive.repository.PaymentRepository paymentRepository;

    public CustomerSecurityService(com.insuradrive.repository.UserRepository userRepository,
                                   com.insuradrive.repository.CustomerRepository customerRepository,
                                   com.insuradrive.repository.InsurancePolicyRepository policyRepository,
                                   com.insuradrive.repository.VehicleRepository vehicleRepository,
                                   com.insuradrive.repository.ClaimRepository claimRepository,
                                   com.insuradrive.repository.PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.policyRepository = policyRepository;
        this.vehicleRepository = vehicleRepository;
        this.claimRepository = claimRepository;
        this.paymentRepository = paymentRepository;
    }

    public Customer getAuthenticatedCustomer(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found: " + username));

        return customerRepository.findById(user.getUserID())
                .orElseThrow(() -> new AccessDeniedException("Customer profile not found for user: " + username));
    }

    public InsurancePolicy assertPolicyOwnership(Customer customer, Long policyId) {
        InsurancePolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + policyId));

        if (policy.getCustomer() == null || !customer.getUserID().equals(policy.getCustomer().getUserID())) {
            throw new AccessDeniedException("Unauthorized access: you do not own policy " + policy.getPolicyNumber());
        }
        return policy;
    }

    public Vehicle assertVehicleOwnership(Customer customer, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + vehicleId));

        if (vehicle.getCustomer() == null || !customer.getUserID().equals(vehicle.getCustomer().getUserID())) {
            throw new AccessDeniedException("Unauthorized access: you do not own vehicle " + vehicle.getRegistrationNo());
        }
        return vehicle;
    }

    public Claim assertClaimOwnership(Customer customer, Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found with ID: " + claimId));

        if (claim.getPolicy() == null || claim.getPolicy().getCustomer() == null
                || !customer.getUserID().equals(claim.getPolicy().getCustomer().getUserID())) {
            throw new AccessDeniedException("Unauthorized access: you do not own claim " + claim.getClaimNumber());
        }
        return claim;
    }

    public Payment assertPaymentOwnership(Customer customer, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));

        if (payment.getPolicy() == null || payment.getPolicy().getCustomer() == null
                || !customer.getUserID().equals(payment.getPolicy().getCustomer().getUserID())) {
            throw new AccessDeniedException("Unauthorized access: you do not own payment transaction " + payment.getTransactionId());
        }
        return payment;
    }
}
