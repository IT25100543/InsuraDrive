package com.insuradrive.service.factory.customer;

import org.springframework.stereotype.Service;

// Selects the appropriate customer factory based on the account type
// Acts as a factory producer that returns either the individual or corporate customer factory

@Service
public class CustomerFactoryProducer {

    private final IndividualCustomerFactory individualFactory;
    private final com.insuradrive.service.factory.customer.CorporateCustomerFactory corporateFactory;

    // Injects the available concrete customer factories
    public CustomerFactoryProducer(
            IndividualCustomerFactory individualFactory,
            com.insuradrive.service.factory.customer.CorporateCustomerFactory corporateFactory
    ) {
        this.individualFactory = individualFactory;
        this.corporateFactory = corporateFactory;
    }

    // Returns the appropriate factory based on the selected account type
    public com.insuradrive.service.factory.customer.CustomerFactory getFactory(com.insuradrive.service.factory.customer.CustomerAccountType type) {

        if (type == com.insuradrive.service.factory.customer.CustomerAccountType.CORPORATE) {
            return corporateFactory;
        }

        return individualFactory;
    }
}