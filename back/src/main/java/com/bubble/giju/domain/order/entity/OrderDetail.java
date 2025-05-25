package com.bubble.giju.domain.order.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_detail")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Long id;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "drink_name", nullable = false)
    private String drinkName;

    @ManyToOne(fetch = FetchType.LAZY) //order 테이블
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    private Order order;

    //취소된 상품 상태
    @Column(name = "is_canceled")
    private boolean canceled;

    //사용자가 이 상품에 대해 환불을 요청했는지
    @Column(name = "is_refund_requested")
    private boolean refundRequested;

    //실제로 관리자에 의해 환불이 완료되었는지
    @Column(name = "is_refunded")
    private boolean refunded;

    public void requestRefund() {
        this.refundRequested = true;
    }

    public void approveRefund() {
        this.refunded = true;
        this.refundRequested = false;
    }


    @Builder
    public OrderDetail(String drinkName, int price, int quantity, Order order, boolean canceled) {
        this.drinkName = drinkName;
        this.price = price;
        this.quantity = quantity;
        this.order = order;
        this.canceled = canceled;
    }

    public void cancel() {
        this.canceled = true;
    }
}
