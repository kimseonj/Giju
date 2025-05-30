package com.bubble.giju.domain.payment.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CanceledItemDto {

    private Long orderDetailId; // 부분 취소일 경우 식별

    @Builder
    public CanceledItemDto(Long orderDetailId) {
        this.orderDetailId = orderDetailId;
    }
}
