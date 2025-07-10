package com.bubble.giju.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DirectOrderRequestDto {
    @Schema(description = "술 Id")
    private Long drinkId;

    @Schema(description = "구매할 수량")
    private int quantity;

    @Builder
    public DirectOrderRequestDto(Long drinkId, int quantity) {
        this.drinkId = drinkId;
        this.quantity = quantity;
    }
}
