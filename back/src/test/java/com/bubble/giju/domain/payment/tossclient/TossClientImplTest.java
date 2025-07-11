package com.bubble.giju.domain.payment.tossclient;

import com.bubble.giju.domain.payment.dto.response.TossCancelResponseDto;
import com.bubble.giju.domain.payment.dto.response.TossPaymentResponseDto;
import com.bubble.giju.domain.payment.tossclient.impl.TossClientImpl;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("TossClientImpl 테스트")
class TossClientImplTest {

    private MockWebServer mockWebServer;
    private TossClientImpl tossClient;
    private ObjectMapper objectMapper;
    private final String TEST_SECRET_KEY = "test_secret_key";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // WebClient 설정
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        // TossClientImpl 인스턴스 생성
        tossClient = new TossClientImpl(webClient);

        // @Value로 주입되는 secret key 설정
        ReflectionTestUtils.setField(tossClient, "tossSecretKey", TEST_SECRET_KEY);

        // ObjectMapper 설정 (JSON 직렬화/역직렬화용)
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("결제 승인 성공 테스트")
    void confirmPayment_Success() throws Exception {
        // Given
        String paymentKey = "test_payment_key";
        String orderId = "test_order_id";
        int amount = 10000;

        TossPaymentResponseDto expectedResponse = createMockPaymentResponse();
        String responseBody = objectMapper.writeValueAsString(expectedResponse);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseBody));

        // When
        TossPaymentResponseDto result = tossClient.confirmPayment(paymentKey, orderId, amount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentKey()).isEqualTo(expectedResponse.getPaymentKey());
        assertThat(result.getOrderId()).isEqualTo(expectedResponse.getOrderId());
        assertThat(result.getTotalAmount()).isEqualTo(expectedResponse.getTotalAmount());
        assertThat(result.getStatus()).isEqualTo(expectedResponse.getStatus());

        // HTTP 요청 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/v1/payments/confirm");
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");

        // Authorization 헤더 검증
        String expectedAuth = "Basic " + Base64.getEncoder()
                .encodeToString((TEST_SECRET_KEY + ":").getBytes());
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo(expectedAuth);

        // 기타 헤더 검증
        assertThat(recordedRequest.getHeader("Accept-Language")).isEqualTo("ko-KR");
        assertThat(recordedRequest.getHeader("Idempotency-Key")).isNotNull();
        assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE)).contains("application/json");

        // 요청 바디 검증
        String requestBody = recordedRequest.getBody().readUtf8();
        Map<String, Object> requestMap = objectMapper.readValue(requestBody, Map.class);
        assertThat(requestMap.get("paymentKey")).isEqualTo(paymentKey);
        assertThat(requestMap.get("orderId")).isEqualTo(orderId);
        assertThat(requestMap.get("amount")).isEqualTo(amount);
    }

    @Test
    @DisplayName("결제 승인 실패 테스트 - 4xx 에러")
    void confirmPayment_Fail_4xxError() {
        // Given
        String paymentKey = "test_payment_key";
        String orderId = "test_order_id";
        int amount = 10000;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"code\":\"INVALID_REQUEST\",\"message\":\"잘못된 요청입니다.\"}"));

        // When & Then
        assertThatThrownBy(() -> tossClient.confirmPayment(paymentKey, orderId, amount))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_CONFIRMATION_FAILED);
    }

    @Test
    @DisplayName("결제 승인 실패 테스트 - 5xx 에러")
    void confirmPayment_Fail_5xxError() {
        // Given
        String paymentKey = "test_payment_key";
        String orderId = "test_order_id";
        int amount = 10000;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 에러입니다.\"}"));

        // When & Then
        assertThatThrownBy(() -> tossClient.confirmPayment(paymentKey, orderId, amount))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_CONFIRMATION_FAILED);
    }

    @Test
    @DisplayName("결제 취소 성공 테스트")
    void cancelPayment_Success() throws Exception {
        // Given
        String paymentKey = "test_payment_key";
        String cancelReason = "고객 요청";
        int cancelAmount = 5000;

        // 실제 Toss API 응답 형태로 JSON 문자열을 직접 만들어 사용
        String responseBody = """
                {
                    "cancels": [
                        {
                            "transactionKey": "test_transaction_key",
                            "cancelReason": "고객 요청",
                            "cancelStatus": "DONE",
                            "cancelAmount": 5000,
                            "canceledAt": "2025-07-01T10:00:00+09:00"
                        }
                    ],
                    "receipt": {
                        "url": "https://dashboard.tosspayments.com/receipt/test"
                    },
                    "cashReceipt": {
                        "receiptUrl": "https://dashboard.tosspayments.com/cash-receipt/test"
                    }
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseBody));

        // When
        TossCancelResponseDto result = tossClient.cancelPayment(paymentKey, cancelReason, cancelAmount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCancels()).isNotEmpty();

        // 최신 취소 정보 검증
        TossCancelResponseDto.CancelDetail latestCancel = result.getLatestCancel();
        assertThat(latestCancel).isNotNull();
        assertThat(latestCancel.getCancelReason()).isEqualTo("고객 요청");
        assertThat(latestCancel.getCancelStatus()).isEqualTo("DONE");
        assertThat(latestCancel.getCancelAmount()).isEqualTo(5000);
        assertThat(latestCancel.getTransactionKey()).isEqualTo("test_transaction_key");
        assertThat(latestCancel.getCanceledAt()).isNotNull();

        // Receipt 정보 검증
        assertThat(result.getReceipt()).isNotNull();
        assertThat(result.getReceipt().getUrl()).isEqualTo("https://dashboard.tosspayments.com/receipt/test");

        // CashReceipt 정보 검증
        assertThat(result.getCashReceipt()).isNotNull();
        assertThat(result.getCashReceipt().getReceiptUrl()).isEqualTo("https://dashboard.tosspayments.com/cash-receipt/test");

        // HTTP 요청 검증
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/v1/payments/" + paymentKey + "/cancel");
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");

        // Authorization 헤더 검증
        String expectedAuth = "Basic " + Base64.getEncoder()
                .encodeToString((TEST_SECRET_KEY + ":").getBytes());
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo(expectedAuth);

        // 요청 바디 검증
        String requestBody = recordedRequest.getBody().readUtf8();
        Map<String, Object> requestMap = objectMapper.readValue(requestBody, Map.class);
        assertThat(requestMap.get("cancelReason")).isEqualTo(cancelReason);
        assertThat(requestMap.get("cancelAmount")).isEqualTo(cancelAmount);
    }


    @Test
    @DisplayName("결제 취소 실패 테스트 - 4xx 에러")
    void cancelPayment_Fail_4xxError() {
        // Given
        String paymentKey = "test_payment_key";
        String cancelReason = "고객 요청";
        int cancelAmount = 5000;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"code\":\"INVALID_REQUEST\",\"message\":\"잘못된 요청입니다.\"}"));

        // When & Then
        assertThatThrownBy(() -> tossClient.cancelPayment(paymentKey, cancelReason, cancelAmount))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_CANCEL_FAILED);
    }

    @Test
    @DisplayName("결제 취소 실패 테스트 - 5xx 에러")
    void cancelPayment_Fail_5xxError() {
        // Given
        String paymentKey = "test_payment_key";
        String cancelReason = "고객 요청";
        int cancelAmount = 5000;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 에러입니다.\"}"));

        // When & Then
        assertThatThrownBy(() -> tossClient.cancelPayment(paymentKey, cancelReason, cancelAmount))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_CANCEL_FAILED);
    }

    private TossPaymentResponseDto createMockPaymentResponse() {
        // TossPaymentResponseDto는 @Getter만 있고 setter가 없으므로
        // 리플렉션을 사용하여 필드 값을 설정합니다.
        TossPaymentResponseDto response = new TossPaymentResponseDto();

        ReflectionTestUtils.setField(response, "paymentKey", "test_payment_key");
        ReflectionTestUtils.setField(response, "orderId", "test_order_id");
        ReflectionTestUtils.setField(response, "totalAmount", 10000);
        ReflectionTestUtils.setField(response, "method", "카드");
        ReflectionTestUtils.setField(response, "status", "DONE");
        ReflectionTestUtils.setField(response, "approvedAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(response, "lastTransactionKey", "test_transaction_key");

        return response;
    }

    private TossCancelResponseDto createMockCancelResponse() {
        // TossCancelResponseDto의 구조를 모르므로 기본적인 필드만 설정
        // 실제 DTO 구조에 맞게 수정해야 합니다.
        TossCancelResponseDto response = new TossCancelResponseDto();

        ReflectionTestUtils.setField(response, "paymentKey", "test_payment_key");
        ReflectionTestUtils.setField(response, "status", "CANCELED");

        return response;
    }
}