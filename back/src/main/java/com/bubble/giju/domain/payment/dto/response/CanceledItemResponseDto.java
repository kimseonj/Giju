package com.bubble.giju.domain.payment.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CanceledItemResponseDto {
    private String drinkName;
    private int price;
    private int quantity;

    @Builder
    public CanceledItemResponseDto(String drinkName, int price, int quantity) {
        this.drinkName = drinkName;
        this.price = price;
        this.quantity = quantity;
    }
}
