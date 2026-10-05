package com.insuradrive.service.factory.customer;

import com.insuradrive.model.Customer;
import com.insuradrive.model.User;

// Factory Method Pattern: Abstract Creator
// Defines the contract for creating related User and Customer objects
// Ensures account credentials and customer profile data are created consistently with the required defaults and roles

public interface CustomerFactory {

    // Creates the User account associated with a customer
    User createUser(
            String fullName,
            String nic,
            String email,
            String rawPassword
    );

    // Creates the Customer profile associated with the User account
    Customer createCustomer(
            User user,
            String fullName,
            String nic,
            String phone,
            String address,
            String drivingLicenseNo,
            String documentName
    );
}