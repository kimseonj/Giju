package com.bubble.giju.domain.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartItemResponseDto {
    @Schema(description = "장바구니 ID")
    private Long cartId;

    @Schema(description = "주류ID")
    private Long drinkId;

    @Schema(description = "주류 이름")
    private String drinkName;

    @Schema(description = "수량")
    private int quantity;

    @Schema(description = "상품 1개 가격")
    private int unitPrice;

    @Schema(description = "상품*수량 = 가격")
    private int totalPrice;

    @Schema(description = "대표 사진url")
    private String imageUrl;

    @Builder
    public CartItemResponseDto (Long cartId, Long drinkId, String drinkName ,int quantity, int unitPrice, int totalPrice, String imageUrl) {
        this.cartId = cartId;
        this.drinkId = drinkId;
        this.drinkName = drinkName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.imageUrl = imageUrl;
    }
}
