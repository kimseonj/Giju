package com.bubble.giju.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class AddToCartRequestDto {

    @NotNull(message = "술 id는 필수")
    private Long drinkId;

    @Min(value = 1, message = "수량은 최소 1개 이상")
    private int quantity;

    @Builder
    public AddToCartRequestDto(Long drinkId, int quantity) {
        this.drinkId = drinkId;
        this.quantity = quantity;
    }
}
