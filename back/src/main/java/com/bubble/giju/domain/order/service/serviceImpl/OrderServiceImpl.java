package com.bubble.giju.domain.order.service.serviceImpl;

import com.bubble.giju.domain.cart.entity.Cart;
import com.bubble.giju.domain.cart.repository.CartRepository;
import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.order.dto.request.RefundRequestDto;
import com.bubble.giju.domain.order.dto.response.*;
import com.bubble.giju.domain.order.entity.Order;
import com.bubble.giju.domain.order.entity.OrderCartMapping;
import com.bubble.giju.domain.order.entity.OrderDetail;
import com.bubble.giju.domain.order.entity.OrderStatus;
import com.bubble.giju.domain.order.repository.OrderCartMappingRepository;
import com.bubble.giju.domain.order.repository.OrderDetailRepository;
import com.bubble.giju.domain.order.repository.OrderRepository;
import com.bubble.giju.domain.order.service.OrderService;
import com.bubble.giju.domain.payment.entity.Payment;
import com.bubble.giju.domain.payment.repository.PaymentRepository;
import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.domain.user.repository.UserRepository;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PaymentRepository paymentRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderCartMappingRepository orderCartMappingRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${toss.success_url}")
    private String successUrl;

    @Value("${toss.fail_url}")
    private String failUrl;


    @Transactional
    @Override
    public OrderResponseDto createOrder(List<Long> cartItemIds, CustomPrincipal principal) {
        User user = userRepository.findById(UUID.fromString(principal.getUserId()))
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_USER));


        List<Cart> cartItems = cartRepository.findAllById(cartItemIds);

        // 총 금액 계산
        int totalAmount = calculateTotalAmount(cartItems);

        // 배달비
        int deliveryCharge = calculateDeliveryCharge(totalAmount);

        // order 이름
        String orderName = buildOrderName(cartItems);

        // order 생성
        Order order = Order.builder()
                .orderName(orderName)
                .totalAmount(totalAmount)
                .deliveryCharge(deliveryCharge)
                .user(user)
                .build();

        // 주문 상세(OrderDetail) 리스트 생성 및 양방향 매핑
        List<OrderDetail> orderDetails = cartItems.stream()
                .map(cart -> {
                    Drink drink = cart.getDrink();
                    return OrderDetail.builder()
                            .drinkName(drink.getName())
                            .price(drink.getPrice() * cart.getQuantity())
                            .quantity(cart.getQuantity())
                            .order(order)
                            .region(drink.getRegion())
                            .build();
                })
                .toList();

        orderDetails.forEach(order::addOrderDetail); // 양방향 연관관계

        // 주문 저장
        Order savedOrder = orderRepository.save(order);

        List<OrderCartMapping> mappings = cartItems.stream()
                .map(cart -> OrderCartMapping.builder()
                        .order(savedOrder)
                        .cart(cart)
                        .build())
                .toList();

        orderCartMappingRepository.saveAll(mappings);

        String tossOrderId = "ORDER_" + savedOrder.getId() + "_" + UUID.randomUUID();
        //String tossOrderId = String.format("ORDER%06d", savedOrder.getId()); //토스페이먼츠서버로 보내기 위해 orderId는 6~64자, 영문 대소문자, 숫자, -, _만 가능 변환

        return OrderResponseDto.builder()
                .orderId( tossOrderId)
                .amount(savedOrder.getTotalAmount() + savedOrder.getDeliveryCharge()) // 상품값 + 배달비
                .orderName(savedOrder.getOrderName())
                .customerEmail(user.getEmail())
                .customerName(user.getName())
                .successUrl(baseUrl + successUrl)
                .failUrl(baseUrl + failUrl)
                .build();
    }

    // 선택된 물 건 총값 계산
    private int calculateTotalAmount(List<Cart> cartItems) {
        return cartItems.stream()
                .mapToInt(Cart::getSubtotal)
                .sum();
    }

    private String buildOrderName(List<Cart> cartItems) {
        List<Drink> drinks = cartItems.stream()
                .map(Cart::getDrink)
                .toList();

        return drinks.size() == 1
                ? drinks.get(0).getName()
                : drinks.get(0).getName() + " 외 " + (drinks.size() - 1) + "개";
    }

    // 3만원 이상 물건 구매시 배달비 무료!
    @Value("${order.delivery-charge}")
    private int deliveryCharge;

    private int calculateDeliveryCharge(int totalAmount) {
        return totalAmount >= 30000 ? 0 : deliveryCharge;
    }


    @Transactional(readOnly = true)
    @Override
    public List<OrderHistoryResonseDto> getOrderHistory(CustomPrincipal principal) {

        User user = userRepository.findById(UUID.fromString(principal.getUserId()))
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_USER));

        // 유저의 주문 목록 중 성공/부분취소만 필터링
        List<Order> orders = orderRepository.findAllByUser(user).stream()
                .filter(order -> {
                    OrderStatus status = order.getOrderStatus();
                    return status == OrderStatus.SUCCEEDED
                            || status == OrderStatus.PARTIALLY_CANCELED
                            || status == OrderStatus.CANCELED;
                })
                .toList();

        // 주문 정보를 DTO로 변환
        return orders.stream()
                .map(order -> {
                    Payment payment = paymentRepository.findByOrder(order)
                            .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

                    List<OrderItemDto> items = order.getOrderDetails().stream()
                            .map(detail -> OrderItemDto.builder()
                                    .drinkName(detail.getDrinkName())
                                    .price(detail.getPrice())
                                    .quantity(detail.getQuantity())
                                    .totalPrice(detail.getPrice() * detail.getQuantity())
                                    .canceled(detail.isCanceled())
                                    .build())
                            .toList();

                    return OrderHistoryResonseDto.builder()
                            .orderId(order.getId())
                            .orderedAt(order.getCreatedAt())
                            .orderStatus(order.getOrderStatus().name())
                            .totalAmount(order.getTotalAmount())
                            .paymentMethod(payment != null ? payment.getPaymentMethod() : "결제 정보 없음")
                            .items(items)
                            .build();
                })
                .toList();
    }


    @Transactional
    public RefundResponseDto requestRefund(RefundRequestDto requestDto, CustomPrincipal principal) {
        UUID userId = UUID.fromString(principal.getUserId());

        // 주문 조회 및 검증
        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_ORDER));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }

        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new CustomException(ErrorCode.CANNOT_REFUND_THIS_ORDER);
        }

        // 환불 요청 대상 조회 및 검증, 여러 개 ID 한번에 조회
        List<OrderDetail> refundItems = orderDetailRepository.findAllById(requestDto.getOrderDetailId());


        // 검증
        boolean isValid = refundItems.stream()
                .allMatch(detail -> detail.getOrder().getId().equals(order.getId()) && // 해당 주문에 속한 주문인지
                        !detail.isCanceled() &&                                        //이미 취소된 상품인지 아닌지
                        !detail.isRefundRequested());                                  //이미 환불 요청된 상품은 아닌지

        if (!isValid) {
            throw new CustomException(ErrorCode.INVALID_REFUND_ITEM);
        }

        // 상태 업데이트 -> 환불이라고 요청했다고 변경
        refundItems.forEach(OrderDetail::requestRefund);

        // 응답 데이터 구성
        List<RefundedItemDto> refundedItemDtos = refundItems.stream()
                .map(item -> RefundedItemDto.builder()
                        .drinkName(item.getDrinkName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        return RefundResponseDto.builder()
                .orderId(order.getId())
                .refundRequestedItems(refundedItemDtos)
                .build();
    }


}
