package com.bubble.giju.domain.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderDetailResponseDto {
    private Long orderId;
    private LocalDateTime orderedAt;
    private String orderStatus;
    private int totalAmount;
    private String paymentMethod;
    private List<OrderItemDto> items;
}
