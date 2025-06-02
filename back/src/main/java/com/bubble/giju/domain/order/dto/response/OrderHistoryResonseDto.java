package com.bubble.giju.domain.order.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class OrderHistoryResonseDto {
    private Long orderId;
    private LocalDateTime orderedAt;
    private String orderStatus;
    private int totalAmount;
    private String paymentMethod;
    private List<OrderItemDto> items;

    @Builder
    public OrderHistoryResonseDto(Long orderId, LocalDateTime orderedAt, String orderStatus,int totalAmount, String paymentMethod, List<OrderItemDto> items) {
        this.orderId = orderId;
        this.orderedAt = orderedAt;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.items = items;
    }
}
