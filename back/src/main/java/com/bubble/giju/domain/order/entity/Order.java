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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "orders") // Order은 sql 예약어 orders로 변경함
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    // BigDecimal 추흐 할인, 쿠폰 등 추가시 변경
    @Column(name = "total_amount" , nullable = false )
    private int totalAmount;

    @CreatedDate // DB생성시 -자동으로 설정
    @Column(name = "created_at" , nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

    @Column(name = "used_cart_ids", columnDefinition = "TEXT")
    private String usedCartIds;

    @ManyToOne(fetch = FetchType.LAZY) //user 테이블
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Payment payment;

    @Builder
    public Order(String orderName ,int totalAmount, int deliveryCharge, User user) {
        this.orderName = orderName;
        this.totalAmount = totalAmount;
        this.deliveryCharge = deliveryCharge;
        this.user = user;
        this.orderStatus = OrderStatus.PENDING;

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

    // 주문 생성 시 사용된 Cart ID 리스트를 JSON 문자열로 저장하는 메서드
    public void setUsedCartIds(List<Long> cartItemIds) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            this.usedCartIds = objectMapper.writeValueAsString(cartItemIds);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_PROCESSING_ERROR);
        }
    }
}
