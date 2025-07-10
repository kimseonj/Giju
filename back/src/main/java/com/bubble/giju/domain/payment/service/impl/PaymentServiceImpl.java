package com.bubble.giju.domain.payment.service.impl;

import com.bubble.giju.domain.cart.entity.Cart;
import com.bubble.giju.domain.cart.repository.CartRepository;
import com.bubble.giju.domain.order.entity.Order;

import com.bubble.giju.domain.order.entity.OrderCartMapping;
import com.bubble.giju.domain.order.entity.OrderDetail;
import com.bubble.giju.domain.order.entity.OrderStatus;

import com.bubble.giju.domain.order.repository.OrderCartMappingRepository;
import com.bubble.giju.domain.order.repository.OrderRepository;
import com.bubble.giju.domain.payment.dto.request.PaymentCancelRequestDto;
import com.bubble.giju.domain.payment.dto.response.*;
import com.bubble.giju.domain.payment.entity.Payment;
import com.bubble.giju.domain.payment.entity.PaymentCancelInfo;
import com.bubble.giju.domain.payment.entity.PaymentFailInfo;
import com.bubble.giju.domain.payment.repository.PaymentCancelInfoRepository;
import com.bubble.giju.domain.payment.repository.PaymentFailInfoRepository;
import com.bubble.giju.domain.payment.repository.PaymentRepository;
import com.bubble.giju.domain.payment.service.PaymentService;
import com.bubble.giju.domain.payment.tossclient.impl.TossClientImpl;
import com.bubble.giju.domain.user.dto.CustomPrincipal;

import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final TossClientImpl tossClientImpl;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentCancelInfoRepository paymentCancelInfoRepository;
    private final PaymentFailInfoRepository paymentFailInfoRepository;
    private final OrderCartMappingRepository orderCartMappingRepository;

    private static final String CANCEL_REASON = "결제 정보 불일치로 인한 자동 취소";
    private final CartRepository cartRepository;


    /**
     * 결제 승인 성공 시 처리 로직
     *
     * - 결제 버튼 클릭 후 결제 승인이 완료되면 Toss 결제 승인 API를 호출해
     *   결제 정보를 검증 및 저장하고,
     * - Order 상태를 'SUCCEEDED'로 변경하며
     * - 장바구니를 비우는 등 후속 처리
     *
     * 주요 단계
     * 1. Order 조회 및 금액 검증
     * 2. soft-delete 여부 확인
     * 3. Toss 결제 승인 API 호출
     * 4. 결제정보(Payment) 저장
     * 5. 추가 검증 실패 시 결제 취소 처리
     * 6. 주문 상태 업데이트
     * 7. 장바구니 항목 정리
     *
     * @param paymentKey   TossPayments에서 발급된 결제 키
     * @param orderId      클라이언트가 전달한 주문 ID
     * @param amount       결제 승인된 금액
     */
    @Transactional
    @Override
    public void paymentSuccess(String paymentKey, String orderId, int amount) {


        Order order = findOrderByStringId(orderId);

        // 결제 버튼 누를때 생성된 Order의 총값 과 리다이렉트 파라미터 비교
        if ((order.getTotalAmount()+order.getDeliveryCharge()) != amount) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_VERIFICATION);
        }

        //soft delete 된 건지 판단
        if (order.isDeleted()) {
            throw new CustomException(ErrorCode.ALREADY_DELETED_ORDER);
        }

        // 결제 승인 요청
        log.info("결제 요청들어감");
        TossPaymentResponseDto tossResponse = tossClientImpl.confirmPayment(paymentKey, orderId, amount);
        log.info("결제 요청완료함");

        Payment payment = Payment.builder()
                .paymentKey(tossResponse.getPaymentKey())
                .amount(tossResponse.getTotalAmount())
                .paymentMethod(tossResponse.getMethod())
                .paymentStatus(tossResponse.getStatus())
                .approvedAt(tossResponse.getApprovedAt())
                .transactionKey(tossResponse.getLastTransactionKey())
                .approveNo(tossResponse.getCard() != null ? tossResponse.getCard().getApproveNo() : null)
                .receiptUrl(tossResponse.getReceipt() != null ? tossResponse.getReceipt().getUrl() : null)
                .cashReceiptUrl(tossResponse.getCashReceipt() != null ? tossResponse.getCashReceipt().getReceiptUrl() : null)
                .order(order)
                .build();

        paymentRepository.save(payment);


        //추가 검증
        if (!orderId.equals(tossResponse.getOrderId()) ||
            order.getTotalAmount() != tossResponse.getTotalAmount()) {

            //결제 취소
            TossCancelResponseDto cancelResponse = tossClientImpl.cancelPayment(
                    tossResponse.getPaymentKey(),
                    CANCEL_REASON,
                    tossResponse.getTotalAmount()
            );

            updateCanceledPayment(payment, cancelResponse);

            paymentCancelInfoRepository.save(PaymentCancelInfo.builder()
                    .cancelReason(CANCEL_REASON)
                    .payment(payment)
                    .transactionKey(cancelResponse.getLatestCancel().getTransactionKey())
                    .canceledAt(cancelResponse.getLatestCancel().getCanceledAt())
                    .receiptUrl(cancelResponse.getReceipt() != null ? cancelResponse.getReceipt().getUrl() : null)
                    .cancelAmount(cancelResponse.getLatestCancel().getCancelAmount())
                    .cancelStatus(cancelResponse.getLatestCancel().getCancelStatus())
                    .cashReceiptUrl(cancelResponse.getCashReceipt() != null ? cancelResponse.getCashReceipt().getReceiptUrl() : null)
                    .isFullCancel(true)
                    .build());

            order.updateStatus(OrderStatus.FAILED);
            orderRepository.save(order);

            throw new CustomException(ErrorCode.INVALID_PAYMENT_VERIFICATION);
        }


        // Order주문 상태 변경
        order.updateStatus(OrderStatus.SUCCEEDED);
        orderRepository.save(order);

        //장바구니 정리
        deleteCartsAndMappingsByOrder(order);
    }

    /**
     * 결제 실패 처리 로직
     *
     * - TossPayments 결제 실패 콜백을 처리하는 메서드
     * - 결제가 실패했을 경우 Payment 엔티티와 실패 정보를 저장하고,
     *   해당 주문의 상태를 'FAILED'로 업데이트
     *
     * 주요 단계
     * 1. 주문 조회
     * 2. 실패 상태의 Payment 생성 및 저장
     * 3. 실패 상세 정보-PaymentFailInfo 저장
     * 4. 주문 상태를 FAILED로 변경
     *
     * @param code     TossPayments가 반환한 실패 코드
     * @param message  TossPayments가 반환한 실패 메시지
     * @param orderId  결제가 실패한 주문 ID
     */
    @Transactional
    @Override
    public void paymentFail(String code, String message, String orderId) {

        Order order = findOrderByStringId(orderId);

        // 실패 상태의 Payment 생성
        Payment payment = Payment.builder()
                .paymentKey(null) // 실패시에 paymentKey 없음
                .amount(order.getTotalAmount())
                .paymentMethod("PENDING")
                .paymentStatus("FAILED")
                .order(order)
                .build();

        paymentRepository.save(payment);

        // 실패 정보 저장
        PaymentFailInfo failInfo = PaymentFailInfo.builder()
                .failCode(code)
                .failMessage(message)
                .payment(payment)
                .build();

        paymentFailInfoRepository.save(failInfo);

        // 주문 상태도 실패로 변경
        order.updateStatus(OrderStatus.FAILED);
        orderRepository.save(order);
    }

    /**
     * 결제 취소 처리 로직
     *
     * - 사용자가 결제 완료(SUCCEEDED) 상태인 주문에 대해 전액 혹은 부분 취소를 요청하면
     *   TossPayments 취소 API를 호출하고,
     *   취소 정보를 기록하며,
     *   주문 상태를 업데이트
     *
     * 주요 단계
     * 1. 사용자 인증 및 주문 조회
     * 2. 주문 상태 검증 (SUCCEEDED 상태만 취소 가능)
     * 3. 결제 정보 조회
     * 4. 취소할 금액 계산
     * 5. TossPayments 결제 취소 API 호출
     * 6. PaymentCancelInfo 생성 및 저장
     * 7. 주문 상태(전체 취소/부분 취소) 업데이트
     * 8. 응답 DTO 반환
     *
     * 반환값
     * - PaymentCancelResponseDto 객체를 반환
     * - 결제 취소 처리 결과 정보를 클라이언트에게 전달
     *   - orderId: 취소 처리된 주문의 ID
     *   - cancelReason: 취소 사유
     *   - cancelAmount: 취소된 금액
     *   - isFullCancel: 전액 취소 여부 (true: 전체 취소, false: 부분 취소)
     *   - canceledAt: 취소 완료 시간
     *   - receiptUrl: 카드 영수증 URL (있으면)
     *   - cashReceiptUrl: 현금 영수증 URL (있으면)
     *
     * @param principal 인증된 사용자 정보
     * @param dto       취소 요청 정보 (주문 ID, 취소 사유, 취소할 항목들)
     * @return PaymentCancelResponseDto 결제 취소 처리 결과
     */
    @Transactional
    @Override
    public PaymentCancelResponseDto paymentCancel(CustomPrincipal principal, PaymentCancelRequestDto dto) {
        UUID userId = UUID.fromString(principal.getUserId());

        Order order = orderRepository.findByIdAndUser_UserId(dto.getOrderId(), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_CANCEL_ACCESS));


        if (order.getOrderStatus() != OrderStatus.SUCCEEDED) {
            throw new CustomException(ErrorCode.CANNOT_CANCEL_THIS_ORDER);
        }

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        int cancelAmount = dto.getCanceledItems().stream()
                .mapToInt(item -> {
                    OrderDetail detail = order.getOrderDetails().stream()
                            .filter(d -> d.getId().equals(item.getOrderDetailId()))
                            .findFirst()
                            .orElseThrow(() -> new CustomException(ErrorCode.ORDER_DETAIL_NOT_FOUND));

                    return detail.getPrice();
                })
                .sum();

        int totalOrderAmount = order.getOrderDetails().stream()
                .mapToInt(OrderDetail::getPrice)
                .sum();

        boolean isFullCancel = (cancelAmount == totalOrderAmount);

        // TossPayments 결제 취소 API 호출
        TossCancelResponseDto tossResponse = tossClientImpl.cancelPayment(
                payment.getPaymentKey(),
                dto.getCancelReason(),
                cancelAmount
        );

        TossCancelResponseDto.CancelDetail cancelDetail = tossResponse.getCancels()
                .get(tossResponse.getCancels().size() - 1);

        // 취소 정보 저장
        PaymentCancelInfo cancelInfo = PaymentCancelInfo.builder()
                .cancelReason(dto.getCancelReason())
                .cancelAmount(cancelAmount)
                .canceledAt(cancelDetail.getCanceledAt())
                .transactionKey(cancelDetail.getTransactionKey())
                .cancelStatus(cancelDetail.getCancelStatus())
                .isFullCancel(isFullCancel)
                .receiptUrl(tossResponse.getReceipt() != null ? tossResponse.getReceipt().getUrl() : null)
                .cashReceiptUrl(tossResponse.getCashReceipt() != null ? tossResponse.getCashReceipt().getReceiptUrl() : null)
                .payment(payment)
                .build();
        paymentCancelInfoRepository.save(cancelInfo);

        // 주문 상태 업데이트
        if (isFullCancel) {
            order.updateStatus(OrderStatus.CANCELED);
        } else {
            order.updateStatus(OrderStatus.PARTIALLY_CANCELED);
        }
        orderRepository.save(order);


        return PaymentCancelResponseDto.builder()
                .orderId(order.getId())
                .cancelReason(cancelInfo.getCancelReason())
                .cancelAmount(cancelInfo.getCancelAmount())
                .isFullCancel(cancelInfo.isFullCancel())
                .canceledAt(cancelInfo.getCanceledAt())
                .receiptUrl(cancelInfo.getReceiptUrl())
                .cashReceiptUrl(cancelInfo.getCashReceiptUrl())
                .build();
    }

    public Order findOrderByStringId(String orderId) {
        String[] parts = orderId.split("_");

        if (parts.length < 3) {
            throw new CustomException(ErrorCode.INVALID_ORDER_ID_FORMAT);
        }

        Long orderIdLong;
        try {
            orderIdLong = Long.parseLong(parts[1]);  // 두 번째 파트가 숫자 ID
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_ORDER_ID_FORMAT);
        }

        return orderRepository.findById(orderIdLong)
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_ORDER));
    }


    private void deleteCartsAndMappingsByOrder(Order order) {
        List<Cart> cartsToDelete = orderCartMappingRepository.findByOrder(order)
                .stream()
                .map(OrderCartMapping::getCart)
                .toList();

        cartRepository.deleteAll(cartsToDelete);
        orderCartMappingRepository.deleteByOrder(order);
    }


    private void updateCanceledPayment(Payment payment, TossCancelResponseDto response) {
        TossCancelResponseDto.CancelDetail latest = response.getLatestCancel();

        payment.cancelWith(
                latest.getTransactionKey(),
                response.getReceipt() != null ? response.getReceipt().getUrl() : null,
                response.getCashReceipt() != null ? response.getCashReceipt().getReceiptUrl() : null
        );

        paymentRepository.save(payment);
    }

}
