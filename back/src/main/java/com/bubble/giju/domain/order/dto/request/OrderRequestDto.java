package com.bubble.giju.domain.order.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderRequestDto {
    private List<Long> cartItemIds;

}
