package com.bubble.giju.domain.order.service;

import com.bubble.giju.domain.cart.entity.Cart;
import com.bubble.giju.domain.cart.repository.CartRepository;
import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.order.dto.response.OrderResponseDto;
import com.bubble.giju.domain.order.entity.Order;
import com.bubble.giju.domain.order.repository.OrderCartMappingRepository;
import com.bubble.giju.domain.order.repository.OrderRepository;
import com.bubble.giju.domain.order.service.serviceImpl.OrderServiceImpl;
import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private CartRepository cartRepository;
    @Mock private OrderCartMappingRepository orderCartMappingRepository;
    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Drink drink1, drink2;
    private Cart cart1, cart2;
    private UUID userId;
    private CustomPrincipal customPrincipal;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = User.builder()
                .userId(userId)
                .loginId("test")
                .password("pass1234")
                .name("테스트 유저")
                .email("test@bubble.com")
                .birthday(LocalDate.of(2000, 1, 1))
                .phoneNumber("01012345678")
                .build();

        customPrincipal = mock(CustomPrincipal.class);
        when(customPrincipal.getUserId()).thenReturn(userId.toString());

        // UUID로 유저 조회 설정
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

        ReflectionTestUtils.setField(orderService, "deliveryCharge", 3000);

        drink1 = Drink.builder().id(1L).name("막걸리").price(15000).build();
        log.info("drink1: name={}, id={}, price={}", drink1.getName(), drink1.getId(), drink1.getPrice());

        drink2 = Drink.builder().id(2L).name("홍주").price(10000).build();
        log.info("drink2: name={}, id={}, price={}", drink2.getName(), drink2.getId(), drink2.getPrice());

        ReflectionTestUtils.setField(orderService, "deliveryCharge", 3000);
        ReflectionTestUtils.setField(orderService, "targetPrice", 30000);

    }

    @Test
    @DisplayName("주문이 정상적으로 생성, 물건값 30000원 이하 배달비 부과")
    void createdOrderAndDeliveryCharge() {
        // given
        log.info("[준비] userId = {}", userId);
        log.info("[준비] user = {}, userId = {}", testUser.getName(),testUser.getUserId());

        log.info("[준비] drink1 = {}, id = {}, price = {}", drink1.getName(), drink1.getId(), drink1.getPrice());
        log.info("[준비] drink2 = {}, id = {}, price = {}", drink2.getName(), drink2.getId(), drink2.getPrice());


        cart1 = Cart.builder().user(testUser).drink(drink1).quantity(1).build(); // 15000
        cart2 = Cart.builder().user(testUser).drink(drink2).quantity(1).build(); // 10000


        log.info("[준비] cart1 = drink: {}, quantity: {}", cart1.getDrink().getName(), cart1.getQuantity());
        log.info("[준비] cart2 = drink: {}, quantity: {}", cart2.getDrink().getName(), cart2.getQuantity());


        given(cartRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(cart1, cart2));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            return order;
        });


        // when
        log.info("[실행] 주문 생성 로직 시작");
        OrderResponseDto result = orderService.createOrder(List.of(1L, 2L), customPrincipal);

        // then
        log.info("[검증] 생성된 Order: totalAmount = {}, user = {}",
                result.getAmount(),  result.getCustomerName());

        assertThat(result.getAmount()).isEqualTo(28000); // 상품 25000 + 배송비 3000
        assertThat(result.getCustomerEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getCustomerName()).isEqualTo(testUser.getName());
        assertThat(result.getOrderName()).contains("외 1개");

        // verify
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(cartRepository).findAllById(List.of(1L, 2L));
    }

    @Test
    @DisplayName("주문이 정상적으로 생성, 물건값 30000원 이상 배달비 무료")
    void createdOrderAndDeliveryChargeFree() {
        // given
        cart1 = Cart.builder().user(testUser).drink(drink1).quantity(2).build(); // 30000
        log.info("Cart 생성: {}", cart1);
        cart2 = Cart.builder().user(testUser).drink(drink2).quantity(2).build(); // 20000
        log.info("Cart 생성: {}", cart2);

        given(cartRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(cart1, cart2));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            return order;
        });
        // when
        log.info("[실행] 주문 생성 로직 시작");
        OrderResponseDto result = orderService.createOrder(List.of(1L, 2L), customPrincipal);

        // then
        log.info("[검증] 생성된 Order: totalAmount = {}, user = {}",
                result.getAmount(),  result.getCustomerName());



        assertThat(result.getAmount()).isEqualTo(50000); // 상품값(50000)+배달비 무료
        assertThat(result.getCustomerEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getCustomerName()).isEqualTo(testUser.getName());
        assertThat(result.getOrderName()).contains("외 1개");

        // verify
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(cartRepository).findAllById(List.of(1L, 2L));
    }
}
