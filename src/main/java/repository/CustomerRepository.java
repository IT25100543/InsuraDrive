package com.insuradrive.repository;

import com.insuradrive.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repository for performing database operations related to Customer entities
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Finds a customer using their NIC
    Optional<Customer> findByNic(String nic);

    // Finds a customer using the NIC/passport value
    @Query("SELECT c FROM Customer c WHERE c.nic = :nicPassport")
    Optional<Customer> findByNicPassport(
            @Param("nicPassport") String nicPassport
    );

    // Searches customers by first or last name, ignoring letter case
    @Query("""
            SELECT c FROM Customer c
            WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
            OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
            """)
    List<Customer> findByFullNameContainingIgnoreCase(
            @Param("name") String name
    );

    // Checks whether a customer already exists with the given NIC/passport
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.nic = :nicPassport")
    boolean existsByNicPassport(
            @Param("nicPassport") String nicPassport
    );

    // Checks whether a customer already exists with the given NIC
    boolean existsByNic(String nic);
}