package com.bubble.giju.domain.payment.tossclient;

import com.bubble.giju.domain.payment.dto.response.TossCancelResponseDto;
import com.bubble.giju.domain.payment.dto.response.TossPaymentResponseDto;

public interface TossClient {
    TossPaymentResponseDto confirmPayment(String paymentKey, String orderId, int amount);
    TossCancelResponseDto cancelPayment(String paymentKey, String cancelReason, int cancelAmount);
}
