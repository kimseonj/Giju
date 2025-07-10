package com.bubble.giju.domain.order.service.serviceImpl;

import com.bubble.giju.domain.cart.entity.Cart;
import com.bubble.giju.domain.cart.repository.CartRepository;
import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.drink.repository.DrinkRepository;
import com.bubble.giju.domain.order.dto.request.DirectOrderRequestDto;
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
    private final DrinkRepository drinkRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${toss.success_url}")
    private String successUrl;

    @Value("${toss.fail_url}")
    private String failUrl;

    @Value("${order.delivery-charge}")
    private int deliveryCharge;

    @Value("${order.targetPrice}")
    private int targetPrice;


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

        //customerkey 생성
        String customerKey = "user-" + user.getUserId();


        // order 생성
        Order order = Order.builder()
                .orderName(orderName)
                .totalAmount(totalAmount)
                .deliveryCharge(deliveryCharge)
                .user(user)
                .customerKey(customerKey)
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
                            .region(String.valueOf(drink.getRegion()))
                            .build();
                })
                .toList();

        orderDetails.forEach(order::addOrderDetail); // 양방향 연관관계

        // 주문 저장
        Order savedOrder = orderRepository.save(order);

        String tossOrderId = "ORDER_" + savedOrder.getId() + "_" + UUID.randomUUID();

        savedOrder.setTossOrderId(tossOrderId);
        orderRepository.save(savedOrder);

        List<OrderCartMapping> mappings = cartItems.stream()
                .map(cart -> OrderCartMapping.builder()
                        .order(savedOrder)
                        .cart(cart)
                        .build())
                .toList();

        orderCartMappingRepository.saveAll(mappings);


        return OrderResponseDto.builder()
                .orderId(tossOrderId)
                .amount(savedOrder.getTotalAmount() + savedOrder.getDeliveryCharge()) // 상품값 + 배달비
                .orderName(savedOrder.getOrderName())
                .customerEmail(user.getEmail())
                .customerName(user.getName())
                .successUrl(baseUrl + successUrl)
                .failUrl(baseUrl + failUrl)
                .build();
    }


    /**
     * 바로구매 결제 상세 정보 조회
     * - 상품 상세 페이지에서 사용자가 수량을 선택 후 "바로구매" 버튼 클릭 시 호출됨
     * - Drink ID와 수량을 기반으로 가격, 배달비, 총 결제금액 계산
     *
     * @param drinkId   구매하려는 전통주 ID
     * @param quantity  구매 수량
     * @param customPrincipal 현재 로그인한 사용자 정보
     * @return DirectOrderInfoDto 결제 상세 정보 응답 객체
     */
    @Transactional(readOnly = true)
    @Override
    public DirectOrderResponseDto getDirectBuyInfo(Long drinkId, int quantity, CustomPrincipal customPrincipal) {

        // 사용자 조회
        User user = userRepository.findById(UUID.fromString(customPrincipal.getUserId()))
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_USER));

        // 전통주 상품 조회
        Drink drink = drinkRepository.findById(drinkId)
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_DRINK));

        // 단가 및 총액 계산
        int pricePerUnit = drink.getPrice();
        int totalPrice = pricePerUnit * quantity;

        // 배달비 계산 (3만 원 이상 무료배송)
        int appliedDeliveryCharge = calculateDeliveryCharge(totalPrice);

        // 결제 상세 응답 DTO 구성
        return DirectOrderResponseDto.builder()
                .drinkId(drink.getId())
                .drinkName(drink.getName())
                .pricePerUnit(pricePerUnit)
                .quantity(quantity)
                .totalPrice(totalPrice)
                .deliveryCharge(appliedDeliveryCharge)
                .totalAmount(totalPrice + appliedDeliveryCharge)
                .build();
    }


    /**
     * [바로 구매 주문 생성 메서드]
     * - 장바구니를 거치지 않고 상품 상세 페이지에서 바로 구매하는 주문을 생성
     *
     * 요청 값
     * - drinkId: 구매할 상품 ID
     * - quantity: 구매 수량
     *
     * 반환 값
     * - tossOrderId : orderId대신 토스 결제를 위한 tossOrderId 반환
     * - amount : 물건 총값 + 택배비
     * - orderName : 주문 이름
     * - customerEmail : 사용자 이메일
     * - customerName : 사용자 이름
     * - successUrl: 결제성공시 리다이렉트 api
     * - failUrl: 결제 실패시 리다이렉트 api
     */
    @Transactional
    @Override
    public OrderResponseDto createDirectOrder(DirectOrderRequestDto directOrderRequestDto, CustomPrincipal principal) {

        // 사용자 조회
        User user = userRepository.findById(UUID.fromString(principal.getUserId()))
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_USER));

        // 상품 조회
        Drink drink = drinkRepository.findById(directOrderRequestDto.getDrinkId())
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_DRINK));

        // 가격 및 배달비 계산
        int totalAmount = drink.getPrice() * directOrderRequestDto.getQuantity();
        int deliveryCharge = calculateDeliveryCharge(totalAmount);

        // 4. 주문 정보 구성
        String orderName = drink.getName();
        String customerKey = "user-" + user.getUserId();

        // 주문 객체 생성
        Order order = Order.builder()
                .orderName(orderName)
                .totalAmount(totalAmount)
                .deliveryCharge(deliveryCharge)
                .user(user)
                .customerKey(customerKey)
                .build();

        // 주문 상세 생성 및 연관관계 매핑
        OrderDetail orderDetail = OrderDetail.builder()
                .drinkName(drink.getName())
                .price(totalAmount)
                .quantity(directOrderRequestDto.getQuantity())
                .order(order)
                .region(String.valueOf(drink.getRegion()))
                .build();

        order.addOrderDetail(orderDetail);
        Order savedOrder = orderRepository.save(order);

        String tossOrderId = "ORDER_" + savedOrder.getId() + "_" + UUID.randomUUID();
        savedOrder.setTossOrderId(tossOrderId);
        orderRepository.save(savedOrder);

        return OrderResponseDto.builder()
                .orderId(tossOrderId)
                .amount(savedOrder.getTotalAmount() + savedOrder.getDeliveryCharge())
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


    private int calculateDeliveryCharge(int totalAmount) {
        return totalAmount >= targetPrice ? 0 : deliveryCharge;
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
                           || status == OrderStatus.DELIVERING
                           || status == OrderStatus.DELIVERED
                           || status == OrderStatus.PARTIALLY_CANCELED
                           || status == OrderStatus.REFUND_REQUESTED
                           || status == OrderStatus.PARTIALLY_REFUND_REQUESTED
                           || status == OrderStatus.REFUNDED
                           || status == OrderStatus.PARTIALLY_REFUNDED;
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
