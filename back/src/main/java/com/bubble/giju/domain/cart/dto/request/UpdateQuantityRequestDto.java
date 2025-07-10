package com.bubble.giju.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateQuantityRequestDto {

    @Min(1)
    private int quantity;

    @Builder
    public UpdateQuantityRequestDto (int quantity) {
        this.quantity = quantity;
    }
}
