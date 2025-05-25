package com.bubble.giju.domain.order.service.serviceImpl;

import com.amazonaws.services.s3.model.GetRequestPaymentConfigurationRequest;
import com.bubble.giju.domain.cart.entity.Cart;
import com.bubble.giju.domain.cart.repository.CartRepository;
import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.order.dto.request.OrderRequestDto;
import com.bubble.giju.domain.order.dto.response.OrderHistoryResonseDto;
import com.bubble.giju.domain.order.dto.response.OrderItemDto;
import com.bubble.giju.domain.order.dto.response.OrderResponseDto;
import com.bubble.giju.domain.order.entity.Order;
import com.bubble.giju.domain.order.entity.OrderDetail;
import com.bubble.giju.domain.order.entity.OrderStatus;
import com.bubble.giju.domain.order.repository.OrderRepository;
import com.bubble.giju.domain.order.service.OrderService;
import com.bubble.giju.domain.payment.entity.Payment;
import com.bubble.giju.domain.payment.repository.PaymentRepository;
import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.domain.user.repository.UserRepository;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import jakarta.persistence.Id;
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

        for (Cart cart : cartItems) {
            Drink drink = cart.getDrink();
            int price = drink.getPrice();
            int quantity = cart.getQuantity();

            // orderDetail 생성
            OrderDetail orderDetail = OrderDetail.builder()
                    .drinkName(drink.getName())
                    .price(price)
                    .quantity(quantity)
                    .order(order)
                    .build();
            //양뱡향 설정
            order.addOrderDetail(orderDetail);
        }
        Order savedOrder = orderRepository.save(order);


        return OrderResponseDto.builder()
                .orderId(savedOrder.getId().toString()) //토스페이먼츠서버로 보내기 위해 String변환
                .amount(savedOrder.getTotalAmount()+savedOrder.getDeliveryCharge()) // 상품값 + 배달비
                .orderName(savedOrder.getOrderName())
                .customerEmail(user.getEmail())
                .customerName(user.getName())
                .successUrl(baseUrl+successUrl)
                .failUrl(baseUrl+failUrl)
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

        // 2. 유저의 주문 목록 중 성공/부분취소만 필터링
        List<Order> orders = orderRepository.findAllByUser(user).stream()
                .filter(order -> {
                    OrderStatus status = order.getOrderStatus();
                    return status == OrderStatus.SUCCEEDED
                            || status == OrderStatus.PARTIALLY_CANCELED
                            || status == OrderStatus.CANCELED;
                })
                .toList();

        // 3. 주문 정보를 DTO로 변환
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
                                    .canceled(detail.isCanceled()) // 필요 시 cancel 정보 매핑
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



    //tossPayment 결제 결과에 따른 order 주문 상태 변화
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_ORDER));
        order.updateStatus(orderStatus);
    }

}
