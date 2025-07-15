package com.bubble.giju.domain.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossCancelResponseDto {

    private List<CancelDetail> cancels;
    private Receipt receipt;
    private CashReceipt cashReceipt;

    public CancelDetail getLatestCancel() {
        return cancels.get(cancels.size() - 1); // 마지막 취소 정보
    }

    @Getter
    public static class CancelDetail {
        private String transactionKey;
        private String cancelReason;
        private String cancelStatus;
        private int cancelAmount;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        private OffsetDateTime canceledAt;
    }

    @Getter
    public static class Receipt {
        private String url;
    }

    @Getter
    public static class CashReceipt {
        private String receiptUrl;
    }
}
