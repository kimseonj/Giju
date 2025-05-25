package com.bubble.giju.domain.order.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RefundRequestDto {
    private Long orderId;
    private List<Long> OrderDetailId;

    @Builder
    public RefundRequestDto(Long orderId, List<Long> OrderDetailId) {
        this.orderId = orderId;
        this.OrderDetailId = OrderDetailId;
    }
}
