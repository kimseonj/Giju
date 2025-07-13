package com.bubble.giju.domain.payment.controller;

import com.bubble.giju.domain.payment.dto.request.CanceledItemDto;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import com.bubble.giju.domain.payment.dto.request.PaymentCancelRequestDto;
import com.bubble.giju.domain.payment.dto.response.PaymentCancelResponseDto;
import com.bubble.giju.domain.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 성공 처리")
    void paymentSuccess() throws Exception {
        // given
        String paymentKey = "test_payment_key_123";
        String orderId = "order_123";
        int amount = 10000;

        doNothing().when(paymentService).paymentSuccess(paymentKey, orderId, amount);

        // when & then
        mockMvc.perform(get("/api/payment/success")
                        .param("paymentKey", paymentKey)
                        .param("orderId", orderId)
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(content().string("결제 성공"));

        // verify
        verify(paymentService).paymentSuccess(paymentKey, orderId, amount);
    }

    @Test
    @DisplayName("결제 실패 처리 - 정상 케이스")
    void paymentFail() throws Exception {
        // given
        String code = "PAYMENT_FAILED";
        String message = "카드 한도 초과";
        String orderId = "order_123";

        doNothing().when(paymentService).paymentFail(code, message, orderId);

        // when & then
        mockMvc.perform(get("/api/payment/fail")
                        .param("code", code)
                        .param("message", message)
                        .param("orderId", orderId))
                .andExpect(status().isOk())
                .andExpect(content().string("결제 실패" + message + ", 주문 아이디" + orderId));
    }

    @Test
    @DisplayName("결제 취소 - 전체 취소 성공")
    @WithMockUser(roles = "USER")
    void cancelPayment_fullCancel() throws Exception {
        // given
        List<CanceledItemDto> canceledItems = List.of(
                CanceledItemDto.builder().orderDetailId(1L).build(),
                CanceledItemDto.builder().orderDetailId(2L).build(),
                CanceledItemDto.builder().orderDetailId(3L).build()
        );

        PaymentCancelRequestDto requestDto = PaymentCancelRequestDto.builder()
                .orderId(123L)
                .canceledItems(canceledItems)
                .cancelReason("단순 변심")
                .build();

        // 서비스가 반환할 응답 DTO
        OffsetDateTime canceledAt = OffsetDateTime.now();

        PaymentCancelResponseDto responseDto = PaymentCancelResponseDto.builder()
                .orderId(123L)
                .cancelAmount(30000)
                .cancelReason("단순 변심")
                .isFullCancel(true)
                .canceledAt(canceledAt)
                .receiptUrl("https://receipt.example.com")
                .cashReceiptUrl("https://cash-receipt.example.com")
                .build();

        when(paymentService.paymentCancel(any(), any(PaymentCancelRequestDto.class)))
                .thenReturn(responseDto);

        // When/Then
        mockMvc.perform(post("/api/payment/cancel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("결제 취소 성공"))
                .andExpect(jsonPath("$.data.orderId").value(123))
                .andExpect(jsonPath("$.data.cancelAmount").value(30000))
                .andExpect(jsonPath("$.data.cancelReason").value("단순 변심"))
                .andExpect(jsonPath("$.data.fullCancel").value(true))
                .andExpect(jsonPath("$.data.canceledAt").exists())
                .andExpect(jsonPath("$.data.receiptUrl").value("https://receipt.example.com"))
                .andExpect(jsonPath("$.data.cashReceiptUrl").value("https://cash-receipt.example.com"));
    }

    @Test
    @DisplayName("결제 취소 - 부분 취소 성공")
    @WithMockUser(roles = "USER")
    void cancelPayment_partialCancel() throws Exception {
        // given
        List<CanceledItemDto> canceledItems = List.of(
                CanceledItemDto.builder().orderDetailId(1L).build(),
                CanceledItemDto.builder().orderDetailId(2L).build()
        );

        PaymentCancelRequestDto requestDto = PaymentCancelRequestDto.builder()
                .orderId(123L)
                .canceledItems(canceledItems)
                .cancelReason("상품 불량")
                .build();

        OffsetDateTime canceledAt = OffsetDateTime.now();

        PaymentCancelResponseDto responseDto = PaymentCancelResponseDto.builder()
                .orderId(123L)
                .cancelAmount(15000)
                .cancelReason("상품 불량")
                .isFullCancel(false)
                .canceledAt(canceledAt)
                .receiptUrl("https://receipt.example.com")
                .cashReceiptUrl("https://cash-receipt.example.com")
                .build();

        when(paymentService.paymentCancel(any(), any(PaymentCancelRequestDto.class)))
                .thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/payment/cancel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("결제 취소 성공"))
                .andExpect(jsonPath("$.data.orderId").value(123))
                .andExpect(jsonPath("$.data.cancelAmount").value(15000))
                .andExpect(jsonPath("$.data.cancelReason").value("상품 불량"))
                .andExpect(jsonPath("$.data.fullCancel").value(false))
                .andExpect(jsonPath("$.data.canceledAt").exists())
                .andExpect(jsonPath("$.data.receiptUrl").value("https://receipt.example.com"))
                .andExpect(jsonPath("$.data.cashReceiptUrl").value("https://cash-receipt.example.com"));
    }

}