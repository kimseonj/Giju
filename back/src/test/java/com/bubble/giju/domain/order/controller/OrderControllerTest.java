package com.bubble.giju.domain.order.controller;

import com.bubble.giju.domain.order.dto.request.DirectOrderRequestDto;
import com.bubble.giju.domain.order.dto.request.OrderRequestDto;
import com.bubble.giju.domain.order.dto.request.RefundRequestDto;
import com.bubble.giju.domain.order.dto.response.*;
import com.bubble.giju.domain.order.service.OrderService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("장바구니 주문 생성")
    @WithMockUser(roles = "USER")
    void createOrder() throws Exception {
        // given
        List<Long> cartIds = List.of(1L, 2L, 3L);
        OrderResponseDto mockResponse = OrderResponseDto.builder()
                .orderId("ORDER_123")
                .amount(35000)
                .orderName("막걸리 외 1개")
                .customerEmail("test@bubble.com")
                .customerName("테스트 유저")
                .successUrl("/success")
                .failUrl("/fail")
                .build();

        when(orderService.createOrder(anyList(), any())).thenReturn(mockResponse);


        // when/then
        mockMvc.perform(post("/api/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderRequestDto(cartIds))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value("ORDER_123"));

        verify(orderService, times(1)).createOrder(eq(cartIds), any());
    }

    @Test
    @DisplayName("바로구매 결제 정보 조회")
    @WithMockUser(roles = "USER")
    void confirmDirect() throws Exception {
        // given
        Long drinkId = 1L;
        int quantity = 2;

        DirectOrderResponseDto responseDto = DirectOrderResponseDto.builder()
                .drinkName("막걸리")
                .drinkId(drinkId)
                .pricePerUnit(8000)
                .quantity(quantity)
                .totalPrice(16000)
                .deliveryCharge(3000)
                .totalAmount(19000)
                .build();

        when(orderService.getDirectBuyInfo(anyLong(), anyInt(), any()))
                .thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/order/confirm-direct")
                        .param("drinkId", String.valueOf(drinkId))
                        .param("quantity", String.valueOf(quantity)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.drinkName").value("막걸리"))
                .andExpect(jsonPath("$.data.drinkId").value(1))
                .andExpect(jsonPath("$.data.pricePerUnit").value(8000))
                .andExpect(jsonPath("$.data.quantity").value(2))
                .andExpect(jsonPath("$.data.totalPrice").value(16000))
                .andExpect(jsonPath("$.data.deliveryCharge").value(3000))
                .andExpect(jsonPath("$.data.totalAmount").value(19000));

        verify(orderService, times(1)).getDirectBuyInfo(eq(drinkId), eq(quantity), any());
    }


    @Test
    @DisplayName("바로구매 주문 생성")
    @WithMockUser(roles = "USER")
    void createDirectOrder() throws Exception {
        // given
        Long drinkId = 1L;
        int quantity = 2;
        DirectOrderRequestDto requestDto = DirectOrderRequestDto.builder()
                .drinkId(drinkId)
                .quantity(quantity)
                .build();

        OrderResponseDto mockResponse = OrderResponseDto.builder()
                .orderId("ORDER_456")
                .amount(30000)
                .orderName("막걸리")
                .customerEmail("test@bubble.com")
                .customerName("테스트 유저")
                .successUrl("/success")
                .failUrl("/fail")
                .build();

        when(orderService.createDirectOrder(any(DirectOrderRequestDto.class), any()))
                .thenReturn(mockResponse);

        // when/then
        mockMvc.perform(post("/api/order/direct")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value("ORDER_456"))
                .andExpect(jsonPath("$.data.amount").value(30000))
                .andExpect(jsonPath("$.data.orderName").value("막걸리"))
                .andExpect(jsonPath("$.data.customerEmail").value("test@bubble.com"))
                .andExpect(jsonPath("$.data.customerName").value("테스트 유저"))
                .andExpect(jsonPath("$.data.successUrl").value("/success"))
                .andExpect(jsonPath("$.data.failUrl").value("/fail"));

        // verify
        verify(orderService, times(1)).createDirectOrder(
                argThat(dto ->
                        dto.getDrinkId().equals(drinkId) &&
                        dto.getQuantity() == quantity
                ),
                any()
        );
    }


    @Test
    @DisplayName("주문 이력 조회")
    @WithMockUser(roles = "USER")
    void orderHistory() throws Exception {
        // given
        OffsetDateTime now = OffsetDateTime.now();

        OrderItemDto item = OrderItemDto.builder()
                .drinkName("막걸리")
                .price(8000)
                .quantity(2)
                .totalPrice(16000)
                .canceled(false)
                .build();

        OrderHistoryResonseDto historyResponse = OrderHistoryResonseDto.builder()
                .orderId(1L)
                .orderedAt(now)
                .orderStatus("SUCCEEDED")
                .totalAmount(19000)
                .paymentMethod("CARD")
                .items(List.of(item))
                .build();

        when(orderService.getOrderHistory(any()))
                .thenReturn(List.of(historyResponse));

        // when/then
        mockMvc.perform(get("/api/order/history"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].orderStatus").value("SUCCEEDED"))
                .andExpect(jsonPath("$[0].totalAmount").value(19000))
                .andExpect(jsonPath("$[0].paymentMethod").value("CARD"))
                // 아이템 리스트 내부 검증
                .andExpect(jsonPath("$[0].items[0].drinkName").value("막걸리"))
                .andExpect(jsonPath("$[0].items[0].price").value(8000))
                .andExpect(jsonPath("$[0].items[0].quantity").value(2))
                .andExpect(jsonPath("$[0].items[0].totalPrice").value(16000))
                .andExpect(jsonPath("$[0].items[0].canceled").value(false));

        // verify
        verify(orderService, times(1)).getOrderHistory(any());
    }

    @Test
    @DisplayName("환불 요청")
    @WithMockUser(roles = "USER")
    void requestRefund() throws Exception {
        // given
        Long orderId = 123L;
        List<Long> orderDetailIdList = List.of(1L, 2L);

        RefundRequestDto requestDto = RefundRequestDto.builder()
                .orderId(orderId)
                .orderDetailId(orderDetailIdList)
                .build();

        RefundedItemDto refundedItem = RefundedItemDto.builder()
                .drinkName("막걸리")
                .quantity(1)
                .price(8000)
                .build();

        RefundResponseDto mockResponse = RefundResponseDto.builder()
                .orderId(orderId)
                .refundRequestedItems(List.of(refundedItem))
                .build();

        when(orderService.requestRefund(any(RefundRequestDto.class), any()))
                .thenReturn(mockResponse);

        // when/then
        mockMvc.perform(post("/api/order/refund/request")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(123))
                .andExpect(jsonPath("$.data.refundRequestedItems[0].drinkName").value("막걸리"))
                .andExpect(jsonPath("$.data.refundRequestedItems[0].quantity").value(1))
                .andExpect(jsonPath("$.data.refundRequestedItems[0].price").value(8000));

        // verify
        verify(orderService, times(1)).requestRefund(
                argThat(dto ->
                        dto.getOrderId().equals(orderId)
                        && dto.getOrderDetailId().equals(orderDetailIdList)
                ),
                any()
        );
    }

}