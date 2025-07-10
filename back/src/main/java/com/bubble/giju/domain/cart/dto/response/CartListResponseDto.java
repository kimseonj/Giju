package com.bubble.giju.domain.cart.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CartListResponseDto {
    private List<CartItemResponseDto> items;
    private int totalPrice;             // 배달비 제외 금액
    private int deliveryCharge;         // 배달비
    private int totalPriceWithDelivery; // 배달비 포함 총액
}
