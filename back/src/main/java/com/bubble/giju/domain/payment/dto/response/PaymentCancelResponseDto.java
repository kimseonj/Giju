package com.bubble.giju.domain.payment.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;


import java.time.OffsetDateTime;

@Getter
@Builder
public class PaymentCancelResponseDto {
    private Long orderId;
    private int cancelAmount;
    private String cancelReason;
    private boolean isFullCancel;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime canceledAt;
    private String receiptUrl;
    private String cashReceiptUrl;
}
