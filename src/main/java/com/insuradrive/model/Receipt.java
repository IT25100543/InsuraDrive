package com.insuradrive.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Maps to table RECEIPT in InsuraDrive_DDD.
 * Linked to PAYMENT(paymentID).
 */
@Entity
@Table(name = "RECEIPT")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receiptID")
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paymentID", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "receiptNumber", length = 50, nullable = false, unique = true)
    private String receiptNumber;

    @Column(name = "generatedDate", nullable = false)
    private LocalDateTime generatedDate = LocalDateTime.now();

    public Receipt() {}

    public Receipt(Long id, Payment payment, String receiptNumber, LocalDateTime generatedDate) {
        this.id = id;
        this.payment = payment;
        this.receiptNumber = receiptNumber;
        this.generatedDate = generatedDate != null ? generatedDate : LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public LocalDateTime getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDateTime generatedDate) { this.generatedDate = generatedDate; }
}

