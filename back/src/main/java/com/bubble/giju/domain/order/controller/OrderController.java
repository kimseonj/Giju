package com.bubble.giju.domain.order.controller;

import com.bubble.giju.domain.cart.dto.response.CartListResponseDto;
import com.bubble.giju.domain.order.dto.request.DirectOrderRequestDto;
import com.bubble.giju.domain.order.dto.request.OrderRequestDto;
import com.bubble.giju.domain.order.dto.request.RefundRequestDto;
import com.bubble.giju.domain.order.dto.response.DirectOrderResponseDto;
import com.bubble.giju.domain.order.dto.response.OrderHistoryResonseDto;
import com.bubble.giju.domain.order.dto.response.OrderResponseDto;
import com.bubble.giju.domain.order.dto.response.RefundResponseDto;
import com.bubble.giju.domain.order.service.OrderService;
import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.global.config.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "주문 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/order")
@PreAuthorize("hasRole('USER')")
public class OrderController {

    private final OrderService orderService;


    @Operation(summary = "장바구니 담고 주문 생성", description = "order, orderDetail 생성, toss연결을 위한 orderId, amount, orderName, customerName, customerEmail, successUrl, failUrl 반환")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
            @RequestBody OrderRequestDto orderRequestDto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        OrderResponseDto orderResponseDto = orderService.createOrder(orderRequestDto.getCartItemIds(), customPrincipal);
        ApiResponse<OrderResponseDto> response = ApiResponse.success("주문 추가 완료", orderResponseDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "바로구매 결제 정보 조회", description = "상품 단건으로 결제 정보를 조회")
    @GetMapping("/confirm-direct")
    public ResponseEntity<ApiResponse<DirectOrderResponseDto>> confirmDirect(
            @RequestParam Long drinkId,
            @RequestParam int quantity,
            @AuthenticationPrincipal CustomPrincipal customPrincipal
    ) {
        DirectOrderResponseDto directBuyInfo = orderService.getDirectBuyInfo(drinkId, quantity, customPrincipal);
        return ResponseEntity.ok(ApiResponse.success("바로구매 결제 정보 조회", directBuyInfo));
    }


    @Operation(summary = "바로구매 주문 생성", description = "상품 단건으로 order/orderDetail 생성")
    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<OrderResponseDto>> createDirectOrder(
            @RequestBody DirectOrderRequestDto directOrderRequestDto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        OrderResponseDto response = orderService.createDirectOrder(directOrderRequestDto, customPrincipal);
        return ResponseEntity.ok(ApiResponse.success("바로구매 주문 생성 완료", response));
    }



    @Operation(summary = "주문 이력 조회", description = "사용자의 전체 주문 이력을 조회합니다")
    @GetMapping("/history")
    public ResponseEntity<List<OrderHistoryResonseDto>> orderHistory(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        List<OrderHistoryResonseDto> history = orderService.getOrderHistory(customPrincipal);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "환불 요청", description = "사용자가 결제 완료된 주문 중 선택된 상품에 대해 환불을 요청, 요청값 : 주문id, 반품할 id리스트")
    @PostMapping("/refund/request")
    public ResponseEntity<ApiResponse<RefundResponseDto>> requestRefund(
            @RequestBody RefundRequestDto requestDto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        RefundResponseDto result = orderService.requestRefund(requestDto, customPrincipal);
        ApiResponse<RefundResponseDto> response = ApiResponse.success("환불 요청이 정상적으로 접수되었습니다", result);
        return ResponseEntity.ok(response);
    }


}
