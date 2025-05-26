package com.bubble.giju.domain.payment.entity;

import com.bubble.giju.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private long id;

    @Column(name = "payment_key", nullable = false, length = 255)
    private String paymentKey;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus;

    @Column(name = "approved_at", nullable = true, columnDefinition = "TIMESTAMP")
    private OffsetDateTime approvedAt;

    @Column(name = "transaction_key")
    private String transactionKey;

    @Column(name = "approve_no")
    private String approveNo;

    // 결제영수증
    @Column(name = "receipt_url")
    private String receiptUrl;

    //Toss에서 결제 수단이 카드가 아닌 계좌이체, 가상계좌, 간편결제 일 경우 영수증
    // 현금영주승
    @Column(name = "cash_receipt_url")
    private String cashReceiptUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Builder
    public Payment(String paymentKey,
                   int amount,
                   String paymentMethod,
                   String paymentStatus,
                   OffsetDateTime approvedAt,
                   String transactionKey,
                   String approveNo,
                   String receiptUrl,
                   String cashReceiptUrl,
                   Order order) {
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.approvedAt = approvedAt;
        this.transactionKey = transactionKey;
        this.approveNo = approveNo;
        this.receiptUrl = receiptUrl;
        this.cashReceiptUrl = cashReceiptUrl;
        this.order = order;
    }

    public void cancelWith(String transactionKey, String receiptUrl, String cashReceiptUrl) {
        this.paymentStatus = "CANCELED";
        this.transactionKey = transactionKey;
        this.receiptUrl = receiptUrl;
        this.cashReceiptUrl = cashReceiptUrl;
    }

}
