package com.bubble.giju.domain.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RefundResponseDto {
    private Long orderId;
    private List<RefundedItemDto> refundRequestedItems;
}