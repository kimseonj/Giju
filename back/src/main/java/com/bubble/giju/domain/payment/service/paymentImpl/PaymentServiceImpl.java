package com.bubble.giju.domain.payment.service.paymentImpl;

import com.bubble.giju.domain.cart.repository.CartRepository;
import com.bubble.giju.domain.order.entity.Order;
import com.bubble.giju.domain.order.entity.OrderDetail;
import com.bubble.giju.domain.order.entity.OrderStatus;
import com.bubble.giju.domain.order.repository.OrderDetailRepository;
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
import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.domain.user.repository.UserRepository;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final TossClientImpl tossClientImpl;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentCancelInfoRepository paymentCancelInfoRepository;
    private final PaymentFailInfoRepository paymentFailInfoRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;

    private static final String CANCEL_REASON = "결제 정보 불일치로 인한 자동 취소";
    private final CartRepository cartRepository;

    @Transactional
    @Override
    public void paymentSuccess(String paymentKey, String orderId, int amount) {

        // TossPayment -> orderId 타입이 Long
        Long orderIdToss = Long.parseLong(orderId);
        // Order 테이블에서 실제 주문 조회
        Order order = getOrder(orderIdToss);

        // 결제 버튼 누를때 생성된 Order의 총값 과 리다이렉트 파라미터 비교
        if (order.getTotalAmount() != amount) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_VERIFICATION);
        }

        // 결제 승인 요청
        TossPaymentResponseDto tossResponse = tossClientImpl.confirmPayment(paymentKey, orderId, amount);


        //추가 검증
        if (!orderId.equals(tossResponse.getOrderId()) ||
                order.getTotalAmount() != tossResponse.getTotalAmount()) {

            // Toss 측에 환불 요청
            //paymentKey, cancelReason, cancelAmount
            tossClientImpl.cancelPayment(
                    tossResponse.getPaymentKey(),
                    CANCEL_REASON,
                    tossResponse.getTotalAmount()
            );

            Payment canceledPayment = saveCanceledPayment(tossResponse, order);
            paymentCancelInfoRepository.save(PaymentCancelInfo.builder()
                    .cancelReason(CANCEL_REASON)
                    .payment(canceledPayment)
                    .build());

            // 이후 예외 처리
            throw new CustomException(ErrorCode.INVALID_PAYMENT_VERIFICATION);
        }

        // Order주문 상태 변경
        order.updateStatus(OrderStatus.SUCCEEDED);
        orderRepository.save(order);

        Payment payment = Payment.builder()
                .paymentKey(tossResponse.getPaymentKey())
                .amount(tossResponse.getTotalAmount())
                .paymentMethod(tossResponse.getMethod())
                .paymentStatus(tossResponse.getStatus())
                .approvedAt(tossResponse.getApprovedAt() != null ? tossResponse.getApprovedAt().toString() : null)
                .order(order)
                .receiptUrl(tossResponse.getReceipt() != null ? tossResponse.getReceipt().getReceiptUrl() : null)
                .cashReceiptUrl(tossResponse.getCashReceipt() != null ? tossResponse.getCashReceipt().getCashReceiptUrl() : null)
                .build();

        paymentRepository.save(payment);

        //결제 성공시 장바구니 삭제
        cartRepository.deleteByUser(order.getUser());
    }


    @Transactional
    @Override
    public void paymentFail(String code, String message, String orderId) {

        Long orderIdToss = Long.parseLong(orderId);
        Order order = getOrder(orderIdToss);

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
    public PaymentCancelResponseDto paymentCancel(PaymentCancelRequestDto paymentCancelRequestDto, CustomPrincipal principal) {
        User user = userRepository.findById(UUID.fromString(principal.getUserId()))
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_USER));

        Order order = getOrder(paymentCancelRequestDto.getOrderId());

        // 사용자 권한 확인
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }

        // 주문 상태 확인
        if (order.getOrderStatus() != OrderStatus.SUCCEEDED) {
            throw new CustomException(ErrorCode.CANNOT_CANCEL_THIS_ORDER);
        }

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(()-> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 취소 금액 합산
        int cancelAmount = paymentCancelRequestDto.getCanceledItmes().stream()
                .mapToInt(CanceledItemDto::getCancelAmount)
                .sum();

        List<Long> canceledIds = paymentCancelRequestDto.getCanceledItmes().stream()
                .map(CanceledItemDto::getOrderDetailId)
                .toList();

        boolean isFullCancel = (cancelAmount == payment.getAmount());

        // 취소요청
        TossCancelResponseDto tossResponse = tossClientImpl.cancelPayment(
                payment.getPaymentKey(),
                paymentCancelRequestDto.getCancelReason(),
                cancelAmount
        );

        /*
        * Toss는 취소할 때마다 이전 취소 이력 + 이번 이력까지 전부 배열로 보내주기 때문에
        * 예 : 상품 4개에서 2개만 취소 후 나머지 2개 추가로 취소 할 경우 그전 이력까지 보여줌
        * cancels.size()-1 처리
        * */
        List<TossCancelInfo> cancels = tossResponse.getCancels();
        TossCancelInfo cancelInfo = cancels.get(cancels.size() - 1);

        PaymentCancelInfo cancelEntity = PaymentCancelInfo.builder()
                .cancelReason(paymentCancelRequestDto.getCancelReason())
                .cancelAmount(cancelAmount)
                .canceledAt(LocalDateTime.parse(cancelInfo.getCanceledAt()))
                .receiptKey(cancelInfo.getReceiptKey())
                .transactionKey(cancelInfo.getTransactionKey())
                .cancelStatus(cancelInfo.getCancelStatus())
                .isFullCancel(isFullCancel)
                .payment(payment)
                .build();

        paymentCancelInfoRepository.save(cancelEntity);

        if (isFullCancel) {
            order.updateStatus(OrderStatus.CANCELED);
        } else {
            order.updateStatus(OrderStatus.PARTIALLY_CANCELED);
        }
        orderRepository.save(order);


        List<OrderDetail> canceledDetails = orderDetailRepository.findAllById(canceledIds);

        boolean isValid = canceledDetails.stream()
                .allMatch(detail -> detail.getOrder().getId().equals(order.getId()));

        for (OrderDetail detail : canceledDetails) {
            detail.cancel();
        }

        if (!isValid) {
            throw new CustomException(ErrorCode.INVALID_CANCEL_ITEM);
        }

        String orderName = canceledDetails.stream()
                .map(OrderDetail::getDrinkName)
                .collect(Collectors.joining(", "));

        List<CanceledItemResponseDto> canceledItemDtos = canceledDetails.stream()
                .map(detail -> CanceledItemResponseDto.builder()
                        .drinkName(detail.getDrinkName())
                        .price(detail.getPrice())
                        .quantity(detail.getQuantity())
                        .build())
                .toList();



        return PaymentCancelResponseDto.builder()
                .orderId(order.getId())
                .orderName(orderName)
                .cancelAmount(cancelAmount)
                .isFullCancel(isFullCancel)
                .canceledAt(cancelEntity.getCanceledAt())
                .receiptUrl("https://merchants.tosspayments.com/web/receipt?receiptKey=" + cancelEntity.getReceiptKey())
                .canceledItems(canceledItemDtos)
                .build();
    }


    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_ORDER));
    }

    private Payment saveCanceledPayment(TossPaymentResponseDto response, Order order) {
        Payment payment = Payment.builder()
                .paymentKey(response.getPaymentKey())
                .amount(response.getTotalAmount())
                .paymentMethod(response.getMethod())
                .paymentStatus("CANCELED")
                .approvedAt(response.getApprovedAt() != null ? response.getApprovedAt().toString() : null)
                .order(order)
                .receiptUrl(response.getReceipt() != null ? response.getReceipt().getReceiptUrl() : null)
                .cashReceiptUrl(response.getCashReceipt() != null ? response.getCashReceipt().getCashReceiptUrl() : null)
                .build();
        return paymentRepository.save(payment);
    }


}
