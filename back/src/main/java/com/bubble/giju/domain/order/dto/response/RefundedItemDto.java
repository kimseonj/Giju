package com.bubble.giju.domain.order.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefundedItemDto {
    private String drinkName;
    private int quantity;
    private int price;
}
