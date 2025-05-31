package com.bubble.giju.cart.service;

import com.bubble.giju.domain.cart.dto.request.AddToCartRequestDto;
import com.bubble.giju.domain.cart.dto.request.UpdateQuantityRequestDto;
import com.bubble.giju.domain.cart.dto.response.CartItemResponseDto;
import com.bubble.giju.domain.cart.dto.response.CartListResponseDto;
import com.bubble.giju.domain.cart.dto.response.CartResponseDto;
import com.bubble.giju.domain.cart.entity.Cart;
import com.bubble.giju.domain.cart.repository.CartRepository;
import com.bubble.giju.domain.cart.service.serviceImpl.CartServiceImpl;
import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.drink.entity.DrinkImage;
import com.bubble.giju.domain.drink.repository.DrinkImageRepository;
import com.bubble.giju.domain.drink.repository.DrinkRepository;
import com.bubble.giju.domain.image.entity.Image;
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

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class) //Mock 사용
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private DrinkRepository drinkRepository;

    @Mock
    private UserRepository userRepository;

    // drinkImageRepository는 일부 테스트에서만 사용되므로
    // 사용되지 않아도 예외가 발생하지 않도록 lenient 옵션을 설정함
    @Mock(lenient = true)
    private DrinkImageRepository drinkImageRepository;


    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Drink testDrink;
    private CustomPrincipal customPrincipal;
    private UUID userId;

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

        testDrink = Drink.builder()
                .id(1L)
                .name("막걸리")
                .price(8000)
                .build();

        // 테스트용 가짜 객세 생성, Spring Security 인증 객체를직접 모킹
        customPrincipal = mock(CustomPrincipal.class);

        // getUserId() 호출 시 특정 UUID 문자열이 반환되도록 설정
        when(customPrincipal.getUserId()).thenReturn(userId.toString());

        // 위에서 반환된 userId로 findById 하면 testUser가 반환
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        Image image = Image.builder()
                .url("https://example.com/test-image.jpg")
                .build();

        DrinkImage mockThumbnail = DrinkImage.builder()
                .drink(testDrink)
                .image(image)
                .isThumbnail(true)
                .build();

        when(drinkImageRepository.findFirstByDrinkAndThumbnailTrue(any()))
                .thenReturn(Optional.of(mockThumbnail));

    }

    public class TestUtil {

        public static void setId(Object target, String fieldName, Object value) {
            try {
                Field field = target.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception e) {
                throw new RuntimeException("ID 설정 실패: " + fieldName, e);
            }
        }

        public static void setId(Object target, Object value) {
            setId(target, "id", value);
        }

        public static <T> void setIdList(List<T> targets, long startId) {
            for (int i = 0; i < targets.size(); i++) {
                setId(targets.get(i), startId + i);
            }
        }
    }



    @Test
    @DisplayName("장바구니 생성, 새로운 물건을 담을 때")
    void newItemAddToCart() throws Exception {
        int count = 2;

        // given
        AddToCartRequestDto requestDto = AddToCartRequestDto.builder()
                .drinkId(1L)
                .quantity(count)
                .build();


        Cart savedCart = Cart.builder()
                .user(testUser)
                .drink(testDrink)
                .quantity(count)
                .build();

//        // Cart 클래스에서 "id"라는 필드를 꺼내고
//        Field idField = Cart.class.getDeclaredField("id");
//        // private 접근이지만 강제로 열어서
//        idField.setAccessible(true);
//
//        // 특정 객체(savedCart)에 id = 1L 값을 집어넣음
//        idField.set(savedCart, 1L); // 테스트용 임의 ID 부여

        TestUtil.setId(savedCart, 1L);

        when(drinkRepository.findById(1L)).thenReturn(Optional.of(testDrink));
        when(cartRepository.findByUserAndDrink(testUser, testDrink)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findAllByUser(testUser)).thenReturn(List.of(savedCart));

        // when
        CartResponseDto response = cartService.addToCart(requestDto, customPrincipal);


        log.info("응답: CartItem = {}", response.getCartItem());
        log.info("응답: Drink ID = {}", response.getCartItem().getDrinkId());
        log.info("응답: Quantity = {}", response.getCartItem().getQuantity());
        log.info("응답: Item Total Price = {}", response.getCartItem().getTotalPrice());
        log.info("응답: Cart Total Price = {}", response.getCartTotalPrice());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCartItem().getDrinkId()).isEqualTo(1L);
        assertThat(response.getCartItem().getQuantity()).isEqualTo(2);
        assertThat(response.getCartItem().getTotalPrice()).isEqualTo(16000); // 8000 * 2
        assertThat(response.getCartTotalPrice()).isEqualTo(16000);
        verify(cartRepository).save(any(Cart.class));
    }



    @Test
    @DisplayName("장바구니에 이미 담긴 물건이 있을 때 수량 증가")
    void addToCart_whenItemAlreadyExists_thenIncreaseQuantity() {
        // given
        AddToCartRequestDto requestDto = AddToCartRequestDto.builder()
                .drinkId(1L)
                .quantity(4)
                .build();

        Cart existingCart = Cart.builder()
                .user(testUser)
                .drink(testDrink)
                .quantity(2) // 기존 수량
                .build();

        TestUtil.setId(existingCart, 1L);


        when(drinkRepository.findById(1L)).thenReturn(Optional.of(testDrink));
        when(cartRepository.findByUserAndDrink(testUser, testDrink)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0)); //save()에 전달된 첫 번째 인자(=0번 인덱스)를 그대로 반환
        Cart updatedCart = Cart.builder().user(testUser).drink(testDrink).quantity(6).build();
        TestUtil.setId(updatedCart, 1L);
        when(cartRepository.findAllByUser(testUser)).thenReturn(List.of(updatedCart));

        log.info("기존 수량: {}", existingCart.getQuantity());

        // when
        CartResponseDto response = cartService.addToCart(requestDto, customPrincipal);

        // then
        assertThat(response.getCartItem().getDrinkId()).isEqualTo(1L);
        assertThat(response.getCartItem().getQuantity()).isEqualTo(6); // 2+4
        log.info("최종 응답 수량: {}", response.getCartItem().getQuantity());
        assertThat(response.getCartItem().getTotalPrice()).isEqualTo(48000); // 8000 * 6
        assertThat(response.getCartTotalPrice()).isEqualTo(48000);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("상품이 여러개 일 때, 개별 상품 값 및 모든상품 총 가격")
    void Item_uniPrice_Cart_totalPrice() {
        //given
        Drink drinkA = Drink.builder()
                .id(1L)
                .name("홍주")
                .price(15000)
                .build();

        Drink drinkB = Drink.builder()
                .id(2L)
                .name("막걸리")
                .price(7000)
                .build();

        Cart cart1 = Cart.builder().user(testUser).drink(drinkA).quantity(2).build(); // 15000 * 2 = 30000
        TestUtil.setId(cart1, 1L);
        Cart cart2 = Cart.builder().user(testUser).drink(drinkB).quantity(1).build();
        TestUtil.setId(cart2, 2L);


        when(drinkRepository.findById(1L)).thenReturn(Optional.of(drinkA));
        when(cartRepository.findByUserAndDrink(testUser, drinkA)).thenReturn(Optional.of(cart1));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findAllByUser(testUser)).thenReturn(List.of(cart1, cart2));


        // when
        AddToCartRequestDto requestDto = AddToCartRequestDto.builder()
                .drinkId(1L)
                .quantity(0)
                .build();

        CartResponseDto response = cartService.addToCart(requestDto, customPrincipal);

        // then
        assertThat(response.getCartItem().getDrinkId()).isEqualTo(1L);
        assertThat(response.getCartItem().getQuantity()).isEqualTo(2);
        assertThat(response.getCartItem().getTotalPrice()).isEqualTo(30000);
        assertThat(response.getCartTotalPrice()).isEqualTo(37000); // 30,000 + 7,000

    }

    @Test
    @DisplayName("상품수량 변경, 증가 시")
    void updateQuantityPlus() {
        //given
        Long cartId = 1L;
        int updateQuantity = 5;

        // 장바구니 임시 생성
        Cart cart = Cart.builder()
                .user(testUser)
                .drink(testDrink)
                .quantity(1)
                .build();

        TestUtil.setId(cart, cartId);

        UpdateQuantityRequestDto updateQuantityRequestDto = UpdateQuantityRequestDto.builder()
                .quantity(updateQuantity)
                .build();


        Cart updatedCart = Cart.builder()
                .user(testUser)
                .drink(testDrink)
                .quantity(updateQuantity)
                .build();

        List<Cart> userCartList = List.of(updatedCart);
        TestUtil.setIdList(userCartList, cartId);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartRepository.findAllByUser(testUser)).thenReturn(userCartList);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);


        // when
        CartResponseDto response = cartService.updateQuantity(cartId, updateQuantityRequestDto, customPrincipal);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCartItem().getQuantity()).isEqualTo(5);
        assertThat(response.getCartItem().getTotalPrice()).isEqualTo(40000);
        assertThat(response.getCartTotalPrice()).isEqualTo(40000);
    }

    @Test
    @DisplayName("상품수량 변경, 감소 시")
    void updateQuantityMinus() {
        //given
        Long cartId = 1L;
        int updateQuantity = 2;

        // 장바구니 임시 생성
        Cart cart = Cart.builder()
                .user(testUser)
                .drink(testDrink)
                .quantity(5)
                .build();
        TestUtil.setId(cart, cartId);

        UpdateQuantityRequestDto updateQuantityRequestDto = UpdateQuantityRequestDto.builder()
                .quantity(updateQuantity)
                .build();


        Cart updatedCart = Cart.builder()
                .user(testUser)
                .drink(testDrink)
                .quantity(updateQuantity)
                .build();

        List<Cart> userCartList = List.of(updatedCart);
        TestUtil.setIdList(userCartList, cartId);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartRepository.findAllByUser(testUser)).thenReturn(userCartList);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);


        // when
        CartResponseDto response = cartService.updateQuantity(cartId, updateQuantityRequestDto, customPrincipal);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCartItem().getQuantity()).isEqualTo(2);
        assertThat(response.getCartItem().getTotalPrice()).isEqualTo(16000);
        assertThat(response.getCartTotalPrice()).isEqualTo(16000);
    }

    @Test
    @DisplayName("선택된 장바구니를 삭제")
    void deleteCartItem() {
        // given
        List<Long> cartIds = List.of(1L, 2L);

        Cart cart1 = Cart.builder()
                .user(testUser)
                .drink(testDrink)
                .quantity(2)
                .build();
        TestUtil.setId(cart1, 1L);


        Cart cart2 = Cart.builder()
                .user(testUser)
                .drink(testDrink)
                .quantity(1)
                .build();

        TestUtil.setId(cart1, 2L);

        List<Cart> mockCartList = List.of(cart1, cart2);


        when(cartRepository.findAllByUser(testUser)).thenReturn(mockCartList);
        when(cartRepository.findAllByUserAndIdIn(testUser, cartIds)).thenReturn(mockCartList);

        // when
        log.info("삭제 전 장바구니 개수: {}", cartRepository.findAllByUser(testUser).size());
        cartService.deleteCartItem(cartIds, customPrincipal);
        // when(cartRepository.findAllByUser(testUser)).thenReturn(List.of());
        log.info("삭제 후 장바구니 개수: {}", cartRepository.findAllByUser(testUser).size());

        // then
        verify(cartRepository, times(1)).deleteAll(mockCartList);
    }

    @Test
    @DisplayName("장바구니에 들어있는 상품들 조회")
    void getCartItems() {
        // given
        List<Long> cartIds = List.of(1L, 2L, 3L);

        Drink drink1 = Drink.builder()
                .id(5L)
                .name("서울의밤")
                .price(15000)
                .build();

        Drink drink2 = Drink.builder()
                .id(6L)
                .name("홍주")
                .price(15000)
                .build();


        Cart cart1 = Cart.builder()
                .user(testUser)
                .drink(drink1)
                .quantity(2)
                .build();
        TestUtil.setId(cart1, 1L);

        Cart cart2 = Cart.builder()
                .user(testUser)
                .drink(drink2)
                .quantity(1)
                .build();
        TestUtil.setId(cart1, 2L);

        Cart cart3 = Cart.builder()
                .user(testUser)
                .drink(testDrink)
                .quantity(1)
                .build();
        TestUtil.setId(cart1, 3L);

        List<Cart> mockCartList = List.of(cart1, cart2, cart3);

        when(cartRepository.findAllByUser(testUser)).thenReturn(mockCartList);

        // when
        CartListResponseDto result = cartService.getCartList(customPrincipal);


        // then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(3);
        assertThat(result.getItems())
                .extracting("drinkName")
                .containsExactlyInAnyOrder("서울의밤", "홍주", "막걸리");

        int expectedTotal = (15000 * 2) + (15000 * 1) + (8000 * 1);
        assertThat(result.getTotalPrice()).isEqualTo(expectedTotal);

        for (CartItemResponseDto item : result.getItems()) {
            log.info("카트 ID: {}, 상품명: {}, 수량: {}, 단가: {}, 총합: {}",
                    item.getCartId(),
                    item.getDrinkName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice());
        }

        log.info("총 금액: {}", result.getTotalPrice());
    }

    @Test
    @DisplayName("선택한 장바구니 ID 중 사용자의 항목만 반환 + 총합 계산")
    void getBuyCartList_onlyUserOwnedCarts() {
        // given
        Drink drink1 = Drink.builder()
                .id(1L).name("막걸리").price(10000).build();
        Drink drink2 = Drink.builder()
                .id(2L).name("청주").price(20000).build();

        Cart ownedCart1 = Cart.builder()
                .user(testUser)
                .drink(drink1)
                .quantity(2) // 총액: 20,000
                .build();
        TestUtil.setId(ownedCart1, 1L);

        Cart ownedCart2 = Cart.builder()
                .user(testUser)
                .drink(drink2)
                .quantity(1) // 총액: 20,000
                .build();
        TestUtil.setId(ownedCart2, 2L);

        // 다른 유저의 카트 (필터링되어야 함)
        User otherUser = User.builder()
                .userId(UUID.randomUUID())
                .loginId("other")
                .build();

        Cart otherCart = Cart.builder()
                .user(otherUser)
                .drink(drink1)
                .quantity(10) // 무시되어야 함
                .build();
        TestUtil.setId(otherCart, 3L);

        List<Long> requestedCartIds = List.of(1L, 2L, 3L); // 3개 중 2개만 내 거

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartRepository.findAllById(requestedCartIds)).thenReturn(List.of(ownedCart1, ownedCart2, otherCart));

        // when
        CartListResponseDto response = cartService.getBuyCartList(requestedCartIds, customPrincipal);

        // then
        assertThat(response.getItems()).hasSize(2); // 내 것만
        assertThat(response.getItems())
                .extracting("drinkName")
                .containsExactlyInAnyOrder("막걸리", "청주");

        assertThat(response.getTotalPrice()).isEqualTo(40000); // 2*10000 + 1*20000
    }

    @Test
    @DisplayName("유저가 가진 카트중 일부 만 선택했을 경우")
    void getBuyCartList_partialSelection() {
        // given
        Drink drink1 = Drink.builder().id(1L).name("막걸리").price(10000).build();
        Drink drink2 = Drink.builder().id(2L).name("청주").price(15000).build();
        Drink drink3 = Drink.builder().id(3L).name("소주").price(5000).build();

        Cart cart1 = Cart.builder().user(testUser).drink(drink1).quantity(1).build(); // 10000
        TestUtil.setId(cart1, 1L);
        Cart cart2 = Cart.builder().user(testUser).drink(drink2).quantity(2).build(); // 30000
        TestUtil.setId(cart2, 2L);
        Cart cart3 = Cart.builder().user(testUser).drink(drink3).quantity(1).build(); // 5000 (선택 안 함)
        TestUtil.setId(cart3, 1L);

        // 사용자가 가진 3개 중 1,2만 선택했다고 가정
        List<Long> selectedCartIds = List.of(1L, 2L);

        // cartRepository는 요청된 ID만 반환해야 함
        when(cartRepository.findAllById(selectedCartIds)).thenReturn(List.of(cart1, cart2));

        // when
        CartListResponseDto result = cartService.getBuyCartList(selectedCartIds, customPrincipal);

        // then
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems())
                .extracting("drinkName")
                .containsExactlyInAnyOrder("막걸리", "청주");

        assertThat(result.getTotalPrice()).isEqualTo(40000); // 10000 + 30000
    }

    @Test
    @DisplayName("장바구니 결제 조회 - 총액이 3만원 이상이면 배달비 0")
    void getBuyCartList_freeDelivery() {
        Cart cart1 = Cart.builder().user(testUser).drink(testDrink).quantity(3).build(); // 8000 * 3 = 24000
        Cart cart2 = Cart.builder().user(testUser).drink(testDrink).quantity(2).build(); // 8000 * 2 = 16000

        List<Cart> selectedCarts = List.of(cart1, cart2);
        TestUtil.setIdList(selectedCarts, 1L);
        List<Long> selectedCartIds = List.of(1L, 2L);

        when(cartRepository.findAllById(selectedCartIds)).thenReturn(selectedCarts);

        // @Value 배달비, 타켓 금액을 수동 주입
        ReflectionTestUtils.setField(cartService, "deliveryCharge", 3000);
        ReflectionTestUtils.setField(cartService, "targetPrice", 30000);

        CartListResponseDto response = cartService.getBuyCartList(selectedCartIds, customPrincipal);

        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getDeliveryCharge()).isEqualTo(0);
        assertThat(response.getTotalPrice()).isEqualTo(40000); // 24000 + 16000
        assertThat(response.getTotalPriceWithDelivery()).isEqualTo(40000);

        for (CartItemResponseDto item : response.getItems()) {
            log.info("카트 ID: {}, 상품명: {}, 수량: {}, 단가: {}, 총합: {}",
                    item.getCartId(),
                    item.getDrinkName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice());
        }
        log.info("상품 총 값 = {}", response.getTotalPrice());
        log.info("배달비 = {}", response.getDeliveryCharge());
        log.info("상품총값 + 배달비= {}", response.getTotalPriceWithDelivery());
    }

    @Test
    @DisplayName("장바구니 결제 조회 - 총액이 3만원 미만이면 배달비 3000")
    void getBuyCartList_withDeliveryFee() {
        Cart cart1 = Cart.builder().user(testUser).drink(testDrink).quantity(2).build(); // 8000 * 2 = 16000
        Cart cart2 = Cart.builder().user(testUser).drink(testDrink).quantity(1).build(); // 8000

        List<Cart> selectedCarts = List.of(cart1, cart2);
        TestUtil.setIdList(selectedCarts, 1L);
        List<Long> selectedCartIds = List.of(1L, 2L);

        when(cartRepository.findAllById(selectedCartIds)).thenReturn(selectedCarts);

        // @Value 배달비, 타켓 금액을 수동 주입
        ReflectionTestUtils.setField(cartService, "deliveryCharge", 3000);
        ReflectionTestUtils.setField(cartService, "targetPrice", 30000);

        CartListResponseDto response = cartService.getBuyCartList(selectedCartIds, customPrincipal);

        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getDeliveryCharge()).isEqualTo(3000);
        assertThat(response.getTotalPrice()).isEqualTo(24000); // 16000 + 8000
        assertThat(response.getTotalPriceWithDelivery()).isEqualTo(27000);


        for (CartItemResponseDto item : response.getItems()) {
            log.info("카트 ID: {}, 상품명: {}, 수량: {}, 단가: {}, 총합: {}",
                    item.getCartId(),
                    item.getDrinkName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice());
        }
        log.info("상품 총 값 = {}", response.getTotalPrice());
        log.info("배달비 = {}", response.getDeliveryCharge());
        log.info("상품총값 + 배달비= {}", response.getTotalPriceWithDelivery());
    }


}
