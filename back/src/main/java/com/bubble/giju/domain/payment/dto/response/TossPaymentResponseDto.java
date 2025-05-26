package com.bubble.giju.domain.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.time.ZonedDateTime;

/*
* 응답 JSON에 정의하지 않은 필드가 있어도 무시하고 넘어감
* Toss API가 새로운 필드를 추가해도 에러 안 남
* 즉, 모르는건 무시해!
* */

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class TossPaymentResponseDto {

    private String paymentKey;
    private String orderId;
    private int totalAmount;
    private String method;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime approvedAt;

    private Receipt receipt;
    private CashReceipt cashReceipt;
    private Card card;
    private String lastTransactionKey;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Receipt {
        private String url;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CashReceipt {
        private String receiptUrl;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Card {
        private String approveNo;
    }
}
