package com.bubble.giju.domain.order.service;

import com.bubble.giju.domain.order.dto.request.RefundRequestDto;
import com.bubble.giju.domain.order.dto.response.OrderHistoryResonseDto;
import com.bubble.giju.domain.order.dto.response.OrderResponseDto;
import com.bubble.giju.domain.order.dto.response.RefundResponseDto;
import com.bubble.giju.domain.user.dto.CustomPrincipal;

import java.util.List;

public interface OrderService {
    OrderResponseDto createOrder(List<Long> cartItemIds, CustomPrincipal customPrincipal);
    List<OrderHistoryResonseDto> getOrderHistory(CustomPrincipal customPrincipal);
    RefundResponseDto requestRefund(RefundRequestDto requestDto, CustomPrincipal principal);

}
