package com.bubble.giju.domain.drink.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DrinkRequestDto {
    @NotBlank(message = "술 이름은 필수입니다.")
    @Schema(description = "술 이름")
    private String name;
    @Schema(description = "가격")
    @Positive(message = "가격은 0보다 커야 합니다.")
    private int price;
    @Schema(description = "재고")
    @PositiveOrZero(message = "재고는 0 이상이어야 합니다.")
    private int stock;
    @Schema(description = "알코올 함유량(%)")
    @DecimalMin(value = "0.0", inclusive = false, message = "알코올 함유량은 0보다 커야 합니다.")
    private double alcoholContent;
    @Schema(description = "용량")
    @Positive(message = "용량은 0보다 커야 합니다.")
    private int volume;
    @Schema(description = "지역")
    @NotBlank(message = "지역은 필수입니다.")
    private String region;
    @Schema(description = "카테고리 Id")
    @Positive(message = "카테고리 ID는 0보다 커야 합니다.")
    private int categoryId;
}
