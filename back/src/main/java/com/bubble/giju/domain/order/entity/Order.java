package com.bubble.giju.domain.order.entity;

import com.bubble.giju.domain.payment.entity.Payment;
import com.bubble.giju.domain.user.entity.User;

import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;


import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders") // Order은 sql 예약어 orders로 변경함
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    // BigDecimal 추흐 할인, 쿠폰 등 추가시 변경
    @Column(name = "total_amount" , nullable = false )
    private int totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private OffsetDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "delivery_charge", nullable = false)
    private int deliveryCharge;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "order_name", nullable = false)
    private String orderName;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    //토스페이머츠 orderid 자릿수 6~64까지
    @Column(name = "toss_order_id", unique = true, length = 64)
    private String tossOrderId;


    /**
     * https://docs.tosspayments.com/sdk/v2/js 공식문서
     * UUID와 같이 충분히 무작위적인 고유 값으로 생성
     * 영문 대소문자, 숫자, 특수문자 -, _, =, ., @ 중 최소 1개를 포함하는
     * 최소 2자 이상 최대 50자 이하의 문자열이어야 함
    **/
    @Column(name = "customer_key", nullable = false, length = 50)

    private String customerKey;


    @ManyToOne(fetch = FetchType.LAZY) //user 테이블
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Payment payment;

    @Builder
    public Order(String orderName ,int totalAmount, int deliveryCharge, User user, String tossOrderId, String customerKey) {
        this.orderName = orderName;
        this.totalAmount = totalAmount;
        this.deliveryCharge = deliveryCharge;
        this.user = user;
        this.orderStatus = OrderStatus.PENDING;
        this.tossOrderId = tossOrderId;
        this.customerKey = customerKey;
        this.createdAt = OffsetDateTime.now(ZoneId.of("Asia/Seoul"));
    }


    public void addOrderDetail(OrderDetail orderDetail) {
        this.orderDetails.add(orderDetail);

    }

    public void updateStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }


    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void setTossOrderId(String tossOrderId) {
        this.tossOrderId = tossOrderId;
    }

}
