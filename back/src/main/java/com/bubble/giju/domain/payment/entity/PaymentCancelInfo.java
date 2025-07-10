package com.bubble.giju.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_cancel_info")
public class PaymentCancelInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancel_id", nullable = false)
    private Long id;

    @Column(name = "cancel_reason", nullable = false)
    private String cancelReason;

    @Column(name = "cancel_amount", nullable = false)
    private int cancelAmount;

    @Column(name = "canceled_at", nullable = false,  columnDefinition = "TIMESTAMP")
    private OffsetDateTime canceledAt;

    @Column(name = "transaction_key")
    private String transactionKey;

    @Column(name = "cancel_status", nullable = false)
    private String cancelStatus;

    @Column(name = "is_full_cancel", nullable = false)
    private boolean isFullCancel;

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "cashReceip_url")
    private String cashReceiptUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, referencedColumnName = "payment_id")
    private Payment payment;

    @Builder
    public PaymentCancelInfo(String cancelReason, int cancelAmount, OffsetDateTime canceledAt,
                             String transactionKey, String cancelStatus, boolean isFullCancel,
                             String receiptUrl, String cashReceiptUrl, Payment payment) {
        this.cancelReason = cancelReason;
        this.cancelAmount = cancelAmount;
        this.canceledAt = canceledAt;
        this.transactionKey = transactionKey;
        this.cancelStatus = cancelStatus;
        this.isFullCancel = isFullCancel;
        this.cashReceiptUrl = cashReceiptUrl;
        this.receiptUrl = receiptUrl;
        this.payment = payment;
    }
}
