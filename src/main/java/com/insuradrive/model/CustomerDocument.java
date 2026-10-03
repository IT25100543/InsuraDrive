package com.insuradrive.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Represents identity verification documents uploaded by customers.
@Entity
@Table(name = "CUSTOMER_DOCUMENT")
public class CustomerDocument {

    // Primary key generated automatically by the database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "documentID")
    private Integer documentID;

    // Many documents can belong to one customer.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerID", nullable = false)
    private Customer customer;

    // Document identification details.
    @Column(name = "documentNumber", length = 50, nullable = false)
    private String documentNumber;

    @Column(name = "documentType", length = 50, nullable = false)
    private String documentType;

    // Date and time when the document was uploaded.
    @Column(name = "uploadDate")
    private LocalDateTime uploadDate = LocalDateTime.now();

    // Current verification state of the document.
    @Column(name = "verificationStatus", length = 30, nullable = false)
    private String verificationStatus = "Pending";

    // Date and time when verification was completed.
    @Column(name = "verifiedDate")
    private LocalDateTime verifiedDate;

    public CustomerDocument() {}

    // Creates a customer document with its main details and verification information.
    public CustomerDocument(
            Integer documentID,
            Customer customer,
            String documentNumber,
            String documentType,
            LocalDateTime uploadDate,
            String verificationStatus,
            LocalDateTime verifiedDate) {

        this.documentID = documentID;
        this.customer = customer;
        this.documentNumber = documentNumber;
        this.documentType = documentType;
        this.uploadDate = uploadDate != null ? uploadDate : LocalDateTime.now();
        this.verificationStatus = verificationStatus != null ? verificationStatus : "Pending";
        this.verifiedDate = verifiedDate;
    }

    // Builder for creating CustomerDocument objects using chained method calls.
    public static CustomerDocumentBuilder builder() {
        return new CustomerDocumentBuilder();
    }

    public static class CustomerDocumentBuilder {

        private Integer documentID;
        private Customer customer;
        private String documentNumber;
        private String documentType;
        private LocalDateTime uploadDate = LocalDateTime.now();
        private String verificationStatus = "Pending";
        private LocalDateTime verifiedDate;

        public CustomerDocumentBuilder documentID(Integer documentID) {
            this.documentID = documentID;
            return this;
        }

        public CustomerDocumentBuilder customer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public CustomerDocumentBuilder documentNumber(String documentNumber) {
            this.documentNumber = documentNumber;
            return this;
        }

        public CustomerDocumentBuilder documentType(String documentType) {
            this.documentType = documentType;
            return this;
        }

        public CustomerDocumentBuilder uploadDate(LocalDateTime uploadDate) {
            this.uploadDate = uploadDate;
            return this;
        }

        public CustomerDocumentBuilder verificationStatus(String verificationStatus) {
            this.verificationStatus = verificationStatus;
            return this;
        }

        public CustomerDocumentBuilder verifiedDate(LocalDateTime verifiedDate) {
            this.verifiedDate = verifiedDate;
            return this;
        }

        public CustomerDocument build() {
            return new CustomerDocument(
                    documentID,
                    customer,
                    documentNumber,
                    documentType,
                    uploadDate,
                    verificationStatus,
                    verifiedDate
            );
        }
    }

    public Integer getDocumentID() {
        return documentID;
    }

    public void setDocumentID(Integer documentID) {
        this.documentID = documentID;
    }

    public Integer getId() {
        return documentID;
    }

    public void setId(Integer id) {
        this.documentID = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getVerifiedDate() {
        return verifiedDate;
    }

    public void setVerifiedDate(LocalDateTime verifiedDate) {
        this.verifiedDate = verifiedDate;
    }

    // Returns true when the document has been verified.
    public boolean isVerified() {
        return "Verified".equalsIgnoreCase(verificationStatus);
    }
}