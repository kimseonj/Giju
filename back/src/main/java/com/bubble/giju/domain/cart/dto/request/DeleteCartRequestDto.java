package com.bubble.giju.domain.cart.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCartRequestDto {

    @NotEmpty(message = "삭제할 장바구니 id 목록은 비어있을 수 없음")
    private List<Long> cartIds;
}
