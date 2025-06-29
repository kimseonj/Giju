package com.bubble.giju.domain.drink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DrinkUpdateRequestDto {

    @Schema(description = "술 가격")
    private int price;
    @Schema(description = "술 재고")
    private int stock;
    @Schema(description = "술 지역")
    private String region;
    @Schema(description = "카테고리 Id 변경")
    private int categoryId;


}