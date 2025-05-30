package com.bubble.giju.domain.payment.service.paymentImpl;

import com.bubble.giju.domain.cart.entity.Cart;
import com.bubble.giju.domain.cart.repository.CartRepository;
import com.bubble.giju.domain.order.entity.Order;

import com.bubble.giju.domain.order.entity.OrderCartMapping;
import com.bubble.giju.domain.order.entity.OrderStatus;

import com.bubble.giju.domain.order.repository.OrderCartMappingRepository;
import com.bubble.giju.domain.order.repository.OrderRepository;
import com.bubble.giju.domain.payment.dto.request.CanceledItemDto;
import com.bubble.giju.domain.payment.dto.request.PaymentCancelRequestDto;
import com.bubble.giju.domain.payment.dto.response.*;
import com.bubble.giju.domain.payment.entity.Payment;
import com.bubble.giju.domain.payment.entity.PaymentCancelInfo;
import com.bubble.giju.domain.payment.entity.PaymentFailInfo;
import com.bubble.giju.domain.payment.repository.PaymentCancelInfoRepository;
import com.bubble.giju.domain.payment.repository.PaymentFailInfoRepository;
import com.bubble.giju.domain.payment.repository.PaymentRepository;
import com.bubble.giju.domain.payment.service.PaymentService;
import com.bubble.giju.domain.payment.tossClient.TossClientImpl.TossClientImpl;
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

    @Transactional
    @Override
    public void paymentSuccess(String paymentKey, String orderId, int amount) {


        Order order = findOrderByStringId(orderId);

        // 결제 버튼 누를때 생성된 Order의 총값 과 리다이렉트 파라미터 비교
        if (order.getTotalAmount() != amount) {
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
                .mapToInt(CanceledItemDto::getCancelAmount)
                .sum();

        boolean isFullCancel = (cancelAmount == payment.getAmount());

        TossCancelResponseDto tossResponse = tossClientImpl.cancelPayment(
                payment.getPaymentKey(),
                dto.getCancelReason(),
                cancelAmount
        );

        TossCancelResponseDto.CancelDetail cancelDetail = tossResponse.getCancels().get(tossResponse.getCancels().size() - 1);

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


    /*  public Order findOrderByStringId(String orderId) {
        // 숫자 아닌 문자 모두 제거
        String orderIdStr = orderId.replaceAll("[^0-9]", "");

        if (orderIdStr.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_ORDER_ID);
        }

        // Long 타입으로 변환
        Long orderIdLong;
        try {
            orderIdLong = Long.parseLong(orderIdStr);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_ORDER_ID_FORMAT);
        }

        // DB에서 주문 조회
        return orderRepository.findById(orderIdLong)
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_ORDER));
    }*/
}
