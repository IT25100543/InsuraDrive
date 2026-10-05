package com.insuradrive.repository;

import com.insuradrive.model.CustomerDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository for accessing and managing CustomerDocument records
@Repository
public interface CustomerDocumentRepository
        extends JpaRepository<CustomerDocument, Integer> {

    // Retrieves all documents uploaded by a specific customer ordered from newest to oldest
    @Query("""
            SELECT cd FROM CustomerDocument cd
            WHERE cd.customer.userID = :customerId
            ORDER BY cd.uploadDate DESC
            """)
    List<CustomerDocument> findByCustomerId(
            @Param("customerId") Long customerId
    );
}