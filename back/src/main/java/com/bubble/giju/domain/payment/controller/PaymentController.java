package com.bubble.giju.domain.payment.controller;

import com.bubble.giju.domain.payment.dto.request.PaymentCancelRequestDto;
import com.bubble.giju.domain.payment.dto.response.PaymentCancelResponseDto;
import com.bubble.giju.domain.payment.service.PaymentService;
import com.bubble.giju.global.config.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "토스페이먼츠 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/payment")
//@PreAuthorize("hasRole('USER')")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 성공 처리", description = "Toss 결제 승인 시 호출되는 콜백 URL")
    @GetMapping("/success")
    public ResponseEntity<String> PaymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam int amount) {
        paymentService.paymentSuccess(paymentKey, orderId, amount);
        return ResponseEntity.ok("결제 성공");
    }

    @Operation(summary = "결제 실패 처리", description = "Toss 결제 실패 시 호출되는 콜백 URL Toss에서 받은 메시지를 그대로 반환")
    @GetMapping("/fail")
    public ResponseEntity<String> paymentFail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam String orderId
    ) {
        paymentService.paymentFail(code, message, orderId);
        return ResponseEntity.ok("결제 실패" + message + ", 주문 아이디" + orderId);
    }

    @Operation(summary = "결제 취소", description = "선택한 상품 또는 전체 결제 금액을 취소 처리, isFullCancel는 전체 취소인지 아닌지 판별용")
    @PostMapping("/{order_id}/cancel")
    public ResponseEntity<ApiResponse<PaymentCancelResponseDto>> cancelPayment(@RequestBody PaymentCancelRequestDto paymentCancelRequestDto) {
        PaymentCancelResponseDto cancel = paymentService.paymentCancel(paymentCancelRequestDto);
        ApiResponse<PaymentCancelResponseDto> response = ApiResponse.success("결제 취소 성공",cancel);
        return ResponseEntity.ok(response);
    }


}
