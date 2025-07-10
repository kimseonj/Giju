package com.bubble.giju.domain.payment.tossclient.impl;

import com.bubble.giju.domain.payment.dto.response.TossCancelResponseDto;
import com.bubble.giju.domain.payment.dto.response.TossPaymentResponseDto;
import com.bubble.giju.domain.payment.tossclient.TossClient;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossClientImpl implements TossClient {

    private final WebClient webClient;

    @Value("${toss.secret-key}")
    private String tossSecretKey;


    @Override
    public TossPaymentResponseDto confirmPayment(String paymentKey, String orderId, int amount) {
        String encodedKey = encodeSecretKey();


        //승인 요청 데이터 확인
        log.info("요청 데이터 → paymentKey: {}, orderId: {}, amount: {}", paymentKey, orderId, amount);
        TossConfirmRequest requestBody = new TossConfirmRequest(paymentKey, orderId, amount);
        log.info("HTTP 요청 바디: {}", requestBody); // record이므로 toString() 자동 사용됨

        return webClient.post()
                .uri("/v1/payments/confirm")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedKey)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("Accept-Language", "ko-KR")
                .contentType(MediaType.APPLICATION_JSON)

                .bodyValue(new TossConfirmRequest(paymentKey, orderId, amount))

                // 요청을 전송하고 응답을 받을 준비
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).map(errorBody -> {
                            log.error(" Toss 결제 승인 실패: {}", errorBody);
                            throw new CustomException(ErrorCode.PAYMENT_CONFIRMATION_FAILED);
                        })
                )
                .bodyToMono(TossPaymentResponseDto.class)
                .block();
    }

    @Override
    public TossCancelResponseDto cancelPayment(String paymentKey, String cancelReason, int cancelAmount) {
        String encodedKey = encodeSecretKey();

        return webClient.post()
                .uri("/v1/payments/" + paymentKey + "/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedKey)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("Accept-Language", "ko-KR")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "cancelReason", cancelReason,
                        "cancelAmount", cancelAmount
                ))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).map(errorBody -> {
                            log.error(" Toss 결제 취소 실패: {}", errorBody);
                            throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
                        })
                )
                .bodyToMono(TossCancelResponseDto.class)
                .block();
    }


    private String encodeSecretKey() {
        return java.util.Base64.getEncoder()
                .encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
    }


    // Toss 결제 승인 요청에 사용되는 내부 전용 DTO
    private record TossConfirmRequest(String paymentKey, String orderId, int amount) {}
}
