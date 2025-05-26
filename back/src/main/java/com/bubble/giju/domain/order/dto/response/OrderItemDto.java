package com.bubble.giju.domain.order.dto.response;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class OrderItemDto {
    private String drinkName;
    private int price;
    private int quantity;
    private int totalPrice;
    private boolean canceled;
}
