package com.bubble.giju.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectOrderResponseDto {

    @Schema(description = "술 이름", example = "막걸리")
    private String drinkName;

    @Schema(description = "술 ID", example = "1")
    private Long drinkId;

    @Schema(description = "단가", example = "8000")
    private int pricePerUnit;

    @Schema(description = "수량", example = "2")
    private int quantity;

    @Schema(description = "상품 총합 (단가 * 수량)", example = "16000")
    private int totalPrice;

    @Schema(description = "배달비", example = "3000")
    private int deliveryCharge;

    @Schema(description = "총 결제 금액 (상품 총합 + 배달비)", example = "19000")
    private int totalAmount;
}
