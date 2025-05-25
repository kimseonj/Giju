package com.bubble.giju.domain.order.service;

import com.bubble.giju.domain.order.dto.request.OrderRequestDto;
import com.bubble.giju.domain.order.dto.response.OrderHistoryResonseDto;
import com.bubble.giju.domain.order.dto.response.OrderResponseDto;
import com.bubble.giju.domain.order.entity.Order;
import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.global.config.CustomException;

import java.util.List;

public interface OrderService {
    OrderResponseDto createOrder(List<Long> cartItemIds, CustomPrincipal customPrincipal);
    List<OrderHistoryResonseDto> getOrderHistory(CustomPrincipal customPrincipal);
}
