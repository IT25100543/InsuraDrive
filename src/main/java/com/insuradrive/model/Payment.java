package com.insuradrive.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Maps to table PAYMENT in InsuraDrive_DDD.
 * Linked to POLICY(policyID).
 * Represents UC-22 (Make Premium Payment).
 */
@Entity
@Table(name = "PAYMENT")
public class Payment {

    public enum PaymentStatus {
        COMPLETED,
        PENDING,
        FAILED,
        REFUNDED
    }

    public enum PaymentType {
        FULL_PAYMENT,
        INSTALLMENT_1,
        INSTALLMENT_2,
        INSTALLMENT_3,
        INSTALLMENT_4
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentID")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policyID", nullable = false)
    private InsurancePolicy policy;

    @Column(name = "dueDate")
    private LocalDate dueDate;

    @Column(name = "paymentMethod", length = 30, nullable = false)
    private String paymentMethod = "Online Card";

    @Column(name = "installmentNo")
    private Integer installmentNo = 1;

    @Column(name = "paymentStatus", length = 30, nullable = false)
    private String paymentStatus = "Paid";

    @Column(name = "paymentDate")
    private LocalDate paymentDate = LocalDate.now();

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Receipt receipt;

    @Transient
    private String transactionId;

    @Transient
    private String receiptNumber;

    @Transient
    private PaymentType paymentType = PaymentType.FULL_PAYMENT;

    @Transient
    private Boolean supervisorVerified = true;

    public Payment() {}

    public Payment(Long id, InsurancePolicy policy, LocalDate dueDate, String paymentMethod,
                   Integer installmentNo, String paymentStatus, LocalDate paymentDate, BigDecimal amount) {
        this.id = id;
        this.policy = policy;
        this.dueDate = dueDate;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Card";
        this.installmentNo = installmentNo != null ? installmentNo : 1;
        this.paymentStatus = paymentStatus != null ? paymentStatus : "Paid";
        this.paymentDate = paymentDate != null ? paymentDate : LocalDate.now();
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    public static class PaymentBuilder {
        private Long id;
        private InsurancePolicy policy;
        private LocalDate dueDate;
        private String paymentMethod = "Online Card";
        private Integer installmentNo = 1;
        private String paymentStatus = "Paid";
        private LocalDate paymentDate = LocalDate.now();
        private BigDecimal amount = BigDecimal.ZERO;
        private String transactionId;
        private String receiptNumber;
        private PaymentType paymentType = PaymentType.FULL_PAYMENT;
        private Boolean supervisorVerified = true;

        public PaymentBuilder id(Long id) { this.id = id; return this; }
        public PaymentBuilder policy(InsurancePolicy policy) { this.policy = policy; return this; }
        public PaymentBuilder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public PaymentBuilder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public PaymentBuilder installmentNo(Integer installmentNo) { this.installmentNo = installmentNo; return this; }
        public PaymentBuilder paymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; return this; }
        public PaymentBuilder status(PaymentStatus status) {
            if (status != null) {
                switch (status) {
                    case COMPLETED -> this.paymentStatus = "Paid";
                    case PENDING -> this.paymentStatus = "Pending";
                    case REFUNDED -> this.paymentStatus = "Refunded";
                    case FAILED -> this.paymentStatus = "Failed";
                }
            }
            return this;
        }
        public PaymentBuilder paymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; return this; }
        public PaymentBuilder paymentDate(LocalDateTime paymentDateTime) {
            this.paymentDate = paymentDateTime != null ? paymentDateTime.toLocalDate() : LocalDate.now();
            return this;
        }
        public PaymentBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public PaymentBuilder amount(Double amt) {
            this.amount = amt != null ? BigDecimal.valueOf(amt) : BigDecimal.ZERO;
            return this;
        }
        public PaymentBuilder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public PaymentBuilder receiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; return this; }
        public PaymentBuilder paymentType(PaymentType paymentType) { this.paymentType = paymentType; return this; }
        public PaymentBuilder supervisorVerified(Boolean supervisorVerified) { this.supervisorVerified = supervisorVerified; return this; }

        public Payment build() {
            Payment p = new Payment(id, policy, dueDate, paymentMethod, installmentNo, paymentStatus, paymentDate, amount);
            p.setTransactionId(transactionId);
            p.setReceiptNumber(receiptNumber);
            p.setPaymentType(paymentType);
            p.setSupervisorVerified(supervisorVerified);
            return p;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InsurancePolicy getPolicy() { return policy; }
    public void setPolicy(InsurancePolicy policy) { this.policy = policy; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Integer getInstallmentNo() { return installmentNo != null ? installmentNo : 1; }
    public void setInstallmentNo(Integer installmentNo) { this.installmentNo = installmentNo; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public PaymentStatus getStatus() {
        if (paymentStatus == null) return PaymentStatus.COMPLETED;
        String s = paymentStatus.trim().toUpperCase();
        if (s.contains("PAID") || s.contains("COMPLET")) return PaymentStatus.COMPLETED;
        if (s.contains("PEND")) return PaymentStatus.PENDING;
        if (s.contains("REFUND")) return PaymentStatus.REFUNDED;
        return PaymentStatus.FAILED;
    }

    public void setStatus(PaymentStatus status) {
        if (status == null) this.paymentStatus = "Paid";
        else {
            switch (status) {
                case COMPLETED -> this.paymentStatus = "Paid";
                case PENDING -> this.paymentStatus = "Pending";
                case REFUNDED -> this.paymentStatus = "Refunded";
                case FAILED -> this.paymentStatus = "Failed";
            }
        }
    }

    public LocalDate getPaymentDateOnly() { return paymentDate; }
    public LocalDateTime getPaymentDate() {
        return paymentDate != null ? paymentDate.atStartOfDay() : LocalDateTime.now();
    }
    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate != null ? paymentDate.toLocalDate() : LocalDate.now();
    }
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate != null ? paymentDate : LocalDate.now();
    }

    public BigDecimal getAmountDecimal() { return amount; }
    public Double getAmount() {
        return amount != null ? amount.doubleValue() : 0.0;
    }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setAmount(Double amount) {
        this.amount = amount != null ? BigDecimal.valueOf(amount) : BigDecimal.ZERO;
    }

    public Receipt getReceipt() { return receipt; }
    public void setReceipt(Receipt receipt) { this.receipt = receipt; }

    public String getTransactionId() {
        if (transactionId != null && !transactionId.isEmpty()) return transactionId;
        return "TXN-2026-" + String.format("%06d", id != null ? id : (int)(Math.random() * 900000 + 100000));
    }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getReceiptNumber() {
        if (receipt != null && receipt.getReceiptNumber() != null) return receipt.getReceiptNumber();
        if (receiptNumber != null && !receiptNumber.isEmpty()) return receiptNumber;
        return "REC-2026-" + String.format("%04d", id != null ? id : (int)(Math.random() * 9000 + 1000));
    }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public PaymentType getPaymentType() { return paymentType != null ? paymentType : PaymentType.FULL_PAYMENT; }
    public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }

    public Boolean getSupervisorVerified() { return supervisorVerified != null ? supervisorVerified : true; }
    public void setSupervisorVerified(Boolean supervisorVerified) { this.supervisorVerified = supervisorVerified; }
}
