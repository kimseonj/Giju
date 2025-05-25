package com.bubble.giju.domain.order.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING("주문대기"),
    SUCCEEDED("결제완료"),
    FAILED("결제실패"),

    DELIVERING("배송중"),
    DELIVERED("배송완료"),

    CANCELED("전체취소"),
    PARTIALLY_CANCELED("부분취소"),

    REFUND_REQUESTED("환불요청됨"),
    PARTIALLY_REFUND_REQUESTED("부분환불요청됨"),

    REFUNDED("전체 환불완료"),
    PARTIALLY_REFUNDED("부분 환불완료");


    private final String label;

}
