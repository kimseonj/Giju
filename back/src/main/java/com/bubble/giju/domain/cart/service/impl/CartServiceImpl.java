package com.bubble.giju.domain.cart.service.impl;

import com.bubble.giju.domain.cart.dto.request.AddToCartRequestDto;
import com.bubble.giju.domain.cart.dto.request.UpdateQuantityRequestDto;
import com.bubble.giju.domain.cart.dto.response.CartListResponseDto;
import com.bubble.giju.domain.cart.dto.response.CartResponseDto;
import com.bubble.giju.domain.cart.dto.response.CartItemResponseDto;
import com.bubble.giju.domain.cart.entity.Cart;
import com.bubble.giju.domain.cart.repository.CartRepository;
import com.bubble.giju.domain.cart.service.CartService;
import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.drink.repository.DrinkImageRepository;
import com.bubble.giju.domain.drink.repository.DrinkRepository;
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
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final DrinkRepository drinkRepository;
    private final UserRepository userRepository;
    private final DrinkImageRepository drinkImageRepository;

    @Value("${order.delivery-charge}")
    private int deliveryCharge;

    @Value("${order.targetPrice}")
    private int targetPrice;


    /**
     * 사용자의 장바구니에 상품을 추가
     * 이미 장바구니에 존재하는 상품이면 수량을 증가
     * 그렇지 않으면 새로 장바구니 항목을 생성
     *
     * @param requestDto 장바구니에 추가할 상품 ID 및 수량 정보
     * @param principal 로그인한 사용자 정보
     * @return 추가된 항목 정보와 전체 장바구니 총합을 담은 응답 DTO
     */
    @Transactional
    @Override
    public CartResponseDto addToCart(AddToCartRequestDto requestDto, CustomPrincipal principal) {


        // 유저 조회
        User user = getCurrentUser(principal);

        // 술 조회
        Drink drink = drinkRepository.findById(requestDto.getDrinkId())
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_DRINK));

        // 기존에 담긴 장바구니에 담긴 상품인지 중복 확인
        Optional<Cart> optionalCart = cartRepository.findByUserAndDrink(user, drink);

        // 기존에 담긴 물건이면 수량 증가 아니면 새로 추가
        Cart cart;
        if (optionalCart.isPresent()) {
            cart = optionalCart.get();
            cart.increaseQuantity(requestDto.getQuantity());
            cartRepository.save(cart);

        } else {
            cart = Cart.builder()
                    .user(user)
                    .drink(drink)
                    .quantity(requestDto.getQuantity())
                    .build();
            cartRepository.save(cart);
        }

        // 카트 총 물건값 계산
        int cartTotalPrice = calculateCartTotalPrice(user);

        CartItemResponseDto cartItemDto = toCartItemDto(cart);


        return CartResponseDto.builder()
                .cartItem(cartItemDto)
                .cartTotalPrice(cartTotalPrice)
                .build();
    }

    /**
     * 사용자의 장바구니에서 특정 항목의 수량을 수정
     * 수량은 1개 이상이어야 하며, 그렇지 않으면 예외를 발생
     *
     * @param id 수정할 장바구니 항목의 ID
     * @param updateQuantityRequestDto 새로운 수량 정보
     * @param principal 로그인한 사용자 정보
     * @return 수정된 장바구니 항목과 장바구니 전체 금액을 포함한 응답 DTO
     */
    @Override
    public CartResponseDto updateQuantity(Long id, UpdateQuantityRequestDto updateQuantityRequestDto, CustomPrincipal principal) {

        // 유저 정보확인
        User user = getCurrentUser(principal);

        // 장바구니 항목 확인
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_CART));

        // 상품 수량 1개 이하가 될수 없게 막음
        if (updateQuantityRequestDto.getQuantity() < 1){
            throw new CustomException(ErrorCode.INVALID_QUANTITY);
        }

        // 수량 업데이트 및 저장
        cart.updateQuantity(updateQuantityRequestDto.getQuantity());
        cartRepository.save(cart);

        // 카트 총 물건값 계산
        int cartTotalPrice = calculateCartTotalPrice(user);
        CartItemResponseDto cartItemDto = toCartItemDto(cart);


        return CartResponseDto.builder()
                .cartItem(cartItemDto)
                .cartTotalPrice(cartTotalPrice)
                .build();
    }

    // 유저의 모든 상품 총값
    private int calculateCartTotalPrice(User user) {
        return cartRepository.findAllByUser(user).stream()
                .mapToInt(c -> c.getDrink().getPrice() * c.getQuantity())
                .sum();
    }

    // 각 상품의 값
    private CartItemResponseDto toCartItemDto(Cart cart) {
        Drink drink = cart.getDrink();

        String imageUrl = drinkImageRepository.findFirstByDrinkAndThumbnailTrue(drink)
                .map(drinkImage -> drinkImage.getImage().getUrl())
                .orElseThrow(() -> new CustomException(ErrorCode.THUMBNAIL_IMAGE_NOT_FOUND));

        return CartItemResponseDto.builder()
                .cartId(cart.getId())
                .drinkId(drink.getId())
                .drinkName(drink.getName())
                .quantity(cart.getQuantity())
                .unitPrice(drink.getPrice())
                .totalPrice(drink.getPrice() * cart.getQuantity())
                .imageUrl(imageUrl)
                .build();
    }

    /**
     * 사용자의 장바구니에서 선택된 항목들을 삭제
     *
     * @param cartId 삭제할 장바구니 항목들의 ID 목록
     * @param principal 로그인한 사용자 정보
     */
    @Override
    public void deleteCartItem(List<Long> cartId, CustomPrincipal principal) {

        // 사용자 조회
        User user = getCurrentUser(principal);

        // 유저정보 + 장바구니id  -> 장바구니 객체 찾기
        List<Cart> cart = cartRepository.findAllByUserAndIdIn(user, cartId);

        // 장바구니 항목 일괄 삭제
        cartRepository.deleteAll(cart);
    }

    /**
     * 로그인한 사용자의 장바구니 목록을 조회
     *
     * @param principal 로그인한 사용자 정보
     * @return 장바구니 항목 리스트 및 총 금액을 포함한 응답 DTO
     */
    @Override
    public CartListResponseDto getCartList(CustomPrincipal principal) {

        // 사용자 조회
        User user = getCurrentUser(principal);

        // 사용자의 장바구니 항목 조회
        List<Cart> carts = cartRepository.findAllByUser(user);

        List<CartItemResponseDto> items = carts.stream()
                .map(this::toCartItemDto)
                .toList();

        // 총 금액 계산
        int totalPrice = calculateCartTotalPrice(user);

        return CartListResponseDto.builder()
                .items(items)
                .totalPrice(totalPrice)
                .build();
    }

    /**
     * 결제를 위해 선택된 장바구니 항목 목록을 조회00
     * 본인 소유의 장바구니 항목만 필터링
     * 배송비 정책에 따라 배송비를 계산하여 함께 반환
     *
     * @param cartIds 결제할 장바구니 항목 ID 목록
     * @param principal 로그인한 사용자 정보
     * @return 선택된 장바구니 항목, 총 가격, 배송비 포함 응답 DTO
     */
    @Transactional
    @Override
    public CartListResponseDto getBuyCartList(List<Long> cartIds, CustomPrincipal principal) {
        // 사용자 조회
        User user = getCurrentUser(principal);

        // 장바구니 조회
        List<Cart> carts = cartRepository.findAllById(cartIds);

        // 본인 소유의 항목만 필터링
        List<Cart> ownedCarts = carts.stream()
                .filter(cart -> cart.getUser().equals(user))
                .toList();

        List<CartItemResponseDto> items = ownedCarts.stream()
                .map(this::toCartItemDto)
                .toList();

        // 총 상품 가격 계산
        int totalPrice = items.stream().mapToInt(CartItemResponseDto::getTotalPrice).sum();

        // 배송비 정책 적용: 일정 금액 이상 무료배송
        int appliedDeliveryCharge = totalPrice >= targetPrice ? 0 : deliveryCharge;

        return CartListResponseDto.builder()
                .items(items)
                .totalPrice(totalPrice)
                .deliveryCharge(appliedDeliveryCharge)
                .totalPriceWithDelivery(totalPrice + appliedDeliveryCharge)
                .build();
    }

    // 유저 찾기
    public User getCurrentUser(CustomPrincipal principal) {
        return userRepository.findById(UUID.fromString(principal.getUserId()))
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_USER));
    }

}
