package com.bubble.giju.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "payment_fail_info")
public class PaymentFailInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fail_id")
    private Long id;

    @Column(name = "fail_message")
    private String failMessage;

    @Column(name = "fail_code")
    private String failCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="payment_id", nullable = false , referencedColumnName = "payment_id")
    private Payment payment;

    @Builder
    public PaymentFailInfo(String failMessage, String failCode, Payment payment) {
        this.failMessage = failMessage;
        this.failCode = failCode;
        this.payment = payment;
    }
}
