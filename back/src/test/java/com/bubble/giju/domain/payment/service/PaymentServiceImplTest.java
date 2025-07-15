package com.bubble.giju.domain.payment.service;

import com.bubble.giju.domain.cart.entity.Cart;
import com.bubble.giju.domain.cart.repository.CartRepository;
import com.bubble.giju.domain.order.entity.Order;
import com.bubble.giju.domain.order.entity.OrderCartMapping;
import com.bubble.giju.domain.order.entity.OrderDetail;
import com.bubble.giju.domain.order.entity.OrderStatus;
import com.bubble.giju.domain.order.repository.OrderCartMappingRepository;
import com.bubble.giju.domain.order.repository.OrderRepository;
import com.bubble.giju.domain.payment.dto.request.CanceledItemDto;
import com.bubble.giju.domain.payment.dto.request.PaymentCancelRequestDto;
import com.bubble.giju.domain.payment.dto.response.PaymentCancelResponseDto;
import com.bubble.giju.domain.payment.dto.response.TossCancelResponseDto;
import com.bubble.giju.domain.payment.dto.response.TossPaymentResponseDto;
import com.bubble.giju.domain.payment.entity.Payment;
import com.bubble.giju.domain.payment.repository.PaymentCancelInfoRepository;
import com.bubble.giju.domain.payment.repository.PaymentFailInfoRepository;
import com.bubble.giju.domain.payment.repository.PaymentRepository;
import com.bubble.giju.domain.payment.service.impl.PaymentServiceImpl;
import com.bubble.giju.domain.payment.tossclient.impl.TossClientImpl;

import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.global.config.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private TossClientImpl tossClientImpl;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentCancelInfoRepository paymentCancelInfoRepository;

    @Mock
    private PaymentFailInfoRepository paymentFailInfoRepository;

    @Mock
    private OrderCartMappingRepository orderCartMappingRepository;

    @Mock
    private CartRepository cartRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("결제 승인 성공 플로우 - 정상 케이스")
    void paymentSuccessProcessApproval() {
        // given
        String paymentKey = "test-payment-key";
        Long orderIdLong = 123L;
        String orderId = "ord_" + orderIdLong + "_confirm";

        int amount = 11000;

        Order mockOrder = mock(Order.class);
        when(mockOrder.getTotalAmount()).thenReturn(10000);
        when(mockOrder.getDeliveryCharge()).thenReturn(1000);
        when(mockOrder.isDeleted()).thenReturn(false);

        when(orderRepository.findById(orderIdLong))
                .thenReturn(Optional.of(mockOrder));

        TossPaymentResponseDto.Receipt receipt = new TossPaymentResponseDto.Receipt();
        ReflectionTestUtils.setField(receipt, "url", "https://receipt.url");

        TossPaymentResponseDto.CashReceipt cashReceipt = new TossPaymentResponseDto.CashReceipt();
        ReflectionTestUtils.setField(cashReceipt, "receiptUrl", "https://cash-receipt.url");

        TossPaymentResponseDto.Card card = new TossPaymentResponseDto.Card();
        ReflectionTestUtils.setField(card, "approveNo", "APPROVE123");

        TossPaymentResponseDto tossResponse = new TossPaymentResponseDto();
        ReflectionTestUtils.setField(tossResponse, "paymentKey", paymentKey);
        ReflectionTestUtils.setField(tossResponse, "orderId", orderId);
        ReflectionTestUtils.setField(tossResponse, "totalAmount", 11000);
        ReflectionTestUtils.setField(tossResponse, "method", "CARD");
        ReflectionTestUtils.setField(tossResponse, "status", "DONE");
        ReflectionTestUtils.setField(tossResponse, "approvedAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(tossResponse, "lastTransactionKey", "tx-123");
        ReflectionTestUtils.setField(tossResponse, "receipt", receipt);
        ReflectionTestUtils.setField(tossResponse, "cashReceipt", cashReceipt);
        ReflectionTestUtils.setField(tossResponse, "card", card);


        when(tossClientImpl.confirmPayment(paymentKey, orderId, amount)).thenReturn(tossResponse);

        // 장바구니 매핑 mock
        OrderCartMapping mapping = mock(OrderCartMapping.class);
        when(mapping.getCart()).thenReturn(mock(Cart.class));
        when(orderCartMappingRepository.findByOrder(mockOrder)).thenReturn(List.of(mapping));

        // when
        paymentService.paymentSuccess(paymentKey, orderId, amount);

        // then
        verify(orderRepository).findById(orderIdLong);
        verify(tossClientImpl).confirmPayment(paymentKey, orderId, amount);
        verify(paymentRepository).save(any(Payment.class));
        verify(orderRepository).save(any(Order.class));

        verify(orderCartMappingRepository).findByOrder(mockOrder);
        verify(cartRepository).deleteAll(anyList());
        verify(orderCartMappingRepository).deleteByOrder(mockOrder);
    }


    @Test
    @DisplayName("결제 실패 - 금액 불일치")
    void paymentSuccessMismatch() {
        // given
        String paymentKey = "test-payment-key";
        Long orderIdLong = 123L;
        String orderId = "ord_" + orderIdLong + "_confirm";

        // 실제 주문은 10000 + 1000 = 11000
        // 결제 승인 받은 amount를 일부러 다르게 설정
        int wrongAmount = 12000;

        // Order mock
        Order mockOrder = mock(Order.class);
        when(mockOrder.getTotalAmount()).thenReturn(10000);
        when(mockOrder.getDeliveryCharge()).thenReturn(1000);
        when(mockOrder.isDeleted()).thenReturn(false);

        when(orderRepository.findById(orderIdLong))
                .thenReturn(Optional.of(mockOrder));

        // when & then
        assertThatThrownBy(() -> paymentService.paymentSuccess(paymentKey, orderId, wrongAmount))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("결제 검증 실패");

        verify(orderRepository).findById(orderIdLong);
        verifyNoInteractions(tossClientImpl);
        verify(paymentRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }


    @Test
    @DisplayName("결제 실패 처리 - 정상 케이스")
    void paymentFailInfo() {
        // given
        String failCode = "FAIL_CODE";
        String failMessage = "실패 사유 예시";
        Long orderIdLong = 123L;
        String orderId = "ord_" + orderIdLong + "_confirm";

        // order mock 객체 생성
        Order mockOrder = mock(Order.class);
        when(mockOrder.getTotalAmount()).thenReturn(10000);

        // orderRepository.findById() 가 orderIdLong으로 조회되도록 mock
        when(orderRepository.findById(orderIdLong)).thenReturn(Optional.of(mockOrder));

        // when
        paymentService.paymentFail(failCode, failMessage, orderId);

        // then
        verify(orderRepository).findById(orderIdLong);

        verify(paymentRepository).save(argThat(payment ->
                payment.getPaymentStatus().equals("FAILED")
                && payment.getPaymentMethod().equals("PENDING")
                && payment.getAmount() == 10000
                && payment.getPaymentKey() == null
                && payment.getOrder() == mockOrder
        ));

        verify(paymentFailInfoRepository).save(argThat(failInfo ->
                failInfo.getFailCode().equals(failCode)
                && failInfo.getFailMessage().equals(failMessage)
                && failInfo.getPayment() != null
        ));

        verify(mockOrder).updateStatus(OrderStatus.FAILED);
        verify(orderRepository).save(mockOrder);
    }

    @Test
    @DisplayName("결제 취소 성공 처리 - 정상 플로우")
    void paymentCancelSuccessfully() {
        // given
        UUID userId = UUID.randomUUID();
        String userIdStr = userId.toString();
        Long orderId = 123L;

        List<CanceledItemDto> canceledItems = List.of(
                new CanceledItemDto(1L), new CanceledItemDto(2L)
        );
        PaymentCancelRequestDto requestDto = PaymentCancelRequestDto.builder()
                .orderId(orderId)
                .cancelReason("사용자 요청")
                .canceledItems(canceledItems)
                .build();

        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getUserId()).thenReturn(userIdStr);

        OrderDetail detail1 = mock(OrderDetail.class);
        when(detail1.getId()).thenReturn(1L);
        when(detail1.getPrice()).thenReturn(5000);

        OrderDetail detail2 = mock(OrderDetail.class);
        when(detail2.getId()).thenReturn(2L);
        when(detail2.getPrice()).thenReturn(5000);

        List<OrderDetail> orderDetails = List.of(detail1, detail2);

        Order mockOrder = mock(Order.class);
        when(mockOrder.getOrderStatus()).thenReturn(OrderStatus.SUCCEEDED);
        when(mockOrder.getOrderDetails()).thenReturn(orderDetails);
        when(mockOrder.getId()).thenReturn(orderId);

        when(orderRepository.findByIdAndUser_UserId(orderId, userId)).thenReturn(Optional.of(mockOrder));

        Payment payment = mock(Payment.class);
        when(payment.getPaymentKey()).thenReturn("payKey");
        when(paymentRepository.findByOrder(mockOrder)).thenReturn(Optional.of(payment));

        OffsetDateTime canceledAt = OffsetDateTime.now();

        TossCancelResponseDto.CancelDetail cancelDetail = new TossCancelResponseDto.CancelDetail();
        ReflectionTestUtils.setField(cancelDetail, "transactionKey", "tx-123");
        ReflectionTestUtils.setField(cancelDetail, "cancelReason", "사용자 요청");
        ReflectionTestUtils.setField(cancelDetail, "cancelStatus", "DONE");
        ReflectionTestUtils.setField(cancelDetail, "cancelAmount", 10000);
        ReflectionTestUtils.setField(cancelDetail, "canceledAt", canceledAt);

        TossCancelResponseDto tossResponse = new TossCancelResponseDto();
        ReflectionTestUtils.setField(tossResponse, "cancels", List.of(cancelDetail));
        ReflectionTestUtils.setField(tossResponse, "receipt", null);
        ReflectionTestUtils.setField(tossResponse, "cashReceipt", null);

        when(tossClientImpl.cancelPayment(any(), any(), anyInt())).thenReturn(tossResponse);


        // when
        PaymentCancelResponseDto result = paymentService.paymentCancel(principal, requestDto);

        // then
        verify(orderRepository).findByIdAndUser_UserId(orderId, userId);
        verify(paymentRepository).findByOrder(mockOrder);
        verify(tossClientImpl).cancelPayment(eq(payment.getPaymentKey()), eq("사용자 요청"), eq(10000));
        verify(paymentCancelInfoRepository).save(any());
        verify(orderRepository).save(mockOrder);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getCancelAmount()).isEqualTo(10000);
        assertThat(result.isFullCancel()).isTrue();
        assertThat(result.getCancelReason()).isEqualTo("사용자 요청");
        assertThat(result.getCanceledAt()).isEqualTo(canceledAt);
    }
    @Test
    @DisplayName("결제 취소 실패 - 주문 상태가 실패")
    void paymentCancelFail() {
        // given
        UUID userId = UUID.randomUUID();
        String userIdStr = userId.toString();
        Long orderId = 123L;

        List<CanceledItemDto> canceledItems = List.of(
                new CanceledItemDto(1L)
        );
        PaymentCancelRequestDto requestDto = PaymentCancelRequestDto.builder()
                .orderId(orderId)
                .cancelReason("사용자 요청")
                .canceledItems(canceledItems)
                .build();

        CustomPrincipal principal = mock(CustomPrincipal.class);
        when(principal.getUserId()).thenReturn(userIdStr);

        Order mockOrder = mock(Order.class);

        // 실패 케이스
        when(mockOrder.getOrderStatus()).thenReturn(OrderStatus.FAILED);
        when(orderRepository.findByIdAndUser_UserId(orderId, userId)).thenReturn(Optional.of(mockOrder));

        // when & then
        assertThatThrownBy(() -> paymentService.paymentCancel(principal, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("해당 주문은 현재 취소할 수 없는 상태입니다");

        verify(orderRepository).findByIdAndUser_UserId(orderId, userId);
        verifyNoInteractions(paymentRepository);
        verifyNoInteractions(tossClientImpl);
    }


}
