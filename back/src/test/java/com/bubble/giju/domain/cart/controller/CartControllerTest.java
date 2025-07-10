package com.bubble.giju.domain.cart.controller;

import com.bubble.giju.domain.cart.dto.request.AddToCartRequestDto;
import com.bubble.giju.domain.cart.dto.request.CartSelectedRequestDto;
import com.bubble.giju.domain.cart.dto.request.DeleteCartRequestDto;
import com.bubble.giju.domain.cart.dto.request.UpdateQuantityRequestDto;
import com.bubble.giju.domain.cart.dto.response.CartItemResponseDto;
import com.bubble.giju.domain.cart.dto.response.CartListResponseDto;
import com.bubble.giju.domain.cart.dto.response.CartResponseDto;
import com.bubble.giju.domain.cart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;
    //가짜 웹 환경을 제공, 실제 웹 서버없이 HTTP요청/응답 테스트

    @Autowired
    private ObjectMapper objectMapper;
    // JSON 직렬화, 역직렬화 담당
    // 요청 dto -> json, json -> 응답 DTO 변환

    @MockitoBean
    private CartService cartService;

    @Test
    @DisplayName("장바구니에 상품 추가")
    @WithMockUser(roles = "USER")
    void addItem() throws Exception {
        // given
        AddToCartRequestDto requestDto = AddToCartRequestDto.builder()
                .drinkId(2L)
                .quantity(2)
                .build();

        CartItemResponseDto cartItem = CartItemResponseDto.builder()
                .cartId(1L)
                .drinkId(2L)
                .drinkName("청주")
                .quantity(2)
                .unitPrice(5000)
                .totalPrice(10000)
                .imageUrl("http://example.com/image.jpg")
                .build();

        CartResponseDto responseDto = CartResponseDto.builder()
                .cartItem(cartItem)
                .cartTotalPrice(10000)
                .build();

        when(cartService.addToCart(any(), any())).thenReturn(responseDto);

        // when/then
        mockMvc.perform(post("/api/cart/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("장바구니에 상품이 추가되었습니다"))
                .andExpect(jsonPath("$.timestamp").exists())

                // Data 안의 cartItem 검증
                .andExpect(jsonPath("$.data.cartItem.cartId").value(1))
                .andExpect(jsonPath("$.data.cartItem.drinkId").value(2))
                .andExpect(jsonPath("$.data.cartItem.drinkName").value("청주"))
                .andExpect(jsonPath("$.data.cartItem.quantity").value(2))
                .andExpect(jsonPath("$.data.cartItem.unitPrice").value(5000))
                .andExpect(jsonPath("$.data.cartItem.totalPrice").value(10000))
                .andExpect(jsonPath("$.data.cartItem.imageUrl").value("http://example.com/image.jpg"))

                // Data 안의 cartTotalPrice 검증
                .andExpect(jsonPath("$.data.cartTotalPrice").value(10000));

    }

    @Test
    @DisplayName("상품 수량 변경")
    @WithMockUser(roles = "USER")
    void updateQuantity() throws Exception {
        // given
        Long cartId = 1L;
        UpdateQuantityRequestDto requestDto = UpdateQuantityRequestDto.builder()
                .quantity(5)
                .build();

        CartItemResponseDto updatedCartItem = CartItemResponseDto.builder()
                .cartId(cartId)
                .drinkId(2L)
                .drinkName("청주")
                .quantity(5) // 변경된 수량
                .unitPrice(5000)
                .totalPrice(25000) // 5 * 5000
                .imageUrl("http://example.com/image.jpg")
                .build();

        CartResponseDto responseDto = CartResponseDto.builder()
                .cartItem(updatedCartItem)
                .cartTotalPrice(25000)
                .build();

        // CustomPrincipal은 any()로 처리
        when(cartService.updateQuantity(eq(cartId), any(UpdateQuantityRequestDto.class), any()))
                .thenReturn(responseDto);

        // when/then
        mockMvc.perform(patch("/api/cart/{id}", cartId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품의 수량이 변경되었습니다"))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.timestamp").exists())

                // Data 안의 cartItem 검증
                .andExpect(jsonPath("$.data.cartItem.cartId").value(1))
                .andExpect(jsonPath("$.data.cartItem.drinkId").value(2))
                .andExpect(jsonPath("$.data.cartItem.drinkName").value("청주"))
                .andExpect(jsonPath("$.data.cartItem.quantity").value(5))
                .andExpect(jsonPath("$.data.cartItem.unitPrice").value(5000))
                .andExpect(jsonPath("$.data.cartItem.totalPrice").value(25000))
                .andExpect(jsonPath("$.data.cartItem.imageUrl").value("http://example.com/image.jpg"))

                // Data 안의 cartTotalPrice 검증
                .andExpect(jsonPath("$.data.cartTotalPrice").value(25000));

        // verify: 서비스 메서드가 올바른 파라미터로 호출되었는지 확인
        verify(cartService).updateQuantity(eq(cartId), any(UpdateQuantityRequestDto.class), any());
    }

    @Test
    @DisplayName("상품 수량 변경 - 유효하지 않은 요청 데이터")
    @WithMockUser(roles = "USER")
    void updateQuantity_InvalidRequestData() throws Exception {
        // given
        Long cartId = 1L;
        String invalidJson = "{}"; // quantity 필드 없음

        // when/then
        mockMvc.perform(patch("/api/cart/{id}", cartId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        // verify: 서비스 메서드가 호출되지 않았는지 확인
        verify(cartService, never()).updateQuantity(any(), any(), any());
    }

    @Test
    @DisplayName("상품 수량 변경 - 잘못된 Content-Type")
    @WithMockUser(roles = "USER")
    void updateQuantity_InvalidContentType() throws Exception {
        // given
        Long cartId = 1L;
        UpdateQuantityRequestDto requestDto = UpdateQuantityRequestDto.builder()
                .quantity(3)
                .build();

        // when/then
        mockMvc.perform(patch("/api/cart/{id}", cartId)
                        .with(csrf())
                        .contentType(MediaType.TEXT_PLAIN) // 잘못된 Content-Type
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnsupportedMediaType());

        // verify: 서비스 메서드가 호출되지 않았는지 확인
        verify(cartService, never()).updateQuantity(any(), any(), any());
    }


    @Test
    @DisplayName("장바구니 삭제")
    @WithMockUser(roles = "USER")
    void deleteCartItem() throws Exception {
        // given
        List<Long> cartIds = Arrays.asList(1L, 2L, 3L);
        DeleteCartRequestDto requestDto = new DeleteCartRequestDto(cartIds);

        // JSON 변환
        String requestJson = objectMapper.writeValueAsString(requestDto);

        doNothing().when(cartService).deleteCartItem(anyList(), any());

        mockMvc.perform(delete("/api/cart/delete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니 항목 삭제 완료"))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists());

        // verify
        verify(cartService).deleteCartItem(eq(cartIds), any());
    }

    @Test
    @DisplayName("장바구니 조회")
    @WithMockUser(roles = "USER")
    void getCartList() throws Exception {
        // given
        CartItemResponseDto cartItem = CartItemResponseDto.builder()
                .cartId(1L)
                .drinkId(2L)
                .drinkName("청주")
                .quantity(2)
                .unitPrice(5000)
                .totalPrice(10000)
                .imageUrl("http://example.com/image.jpg")
                .build();

        CartListResponseDto responseDto = CartListResponseDto.builder()
                .items(List.of(cartItem))
                .totalPrice(10000)
                .deliveryCharge(3000)
                .totalPriceWithDelivery(13000)
                .build();

        when(cartService.getCartList(any())).thenReturn(responseDto);
        // when/then
        mockMvc.perform(get("/api/cart")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].cartId").value(1))
                .andExpect(jsonPath("$.items[0].drinkId").value(2))
                .andExpect(jsonPath("$.items[0].drinkName").value("청주"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(5000))
                .andExpect(jsonPath("$.items[0].totalPrice").value(10000))
                .andExpect(jsonPath("$.items[0].imageUrl").value("http://example.com/image.jpg"))
                .andExpect(jsonPath("$.totalPrice").value(10000))
                .andExpect(jsonPath("$.deliveryCharge").value(3000))
                .andExpect(jsonPath("$.totalPriceWithDelivery").value(13000));

        verify(cartService).getCartList(any());
    }

    @Test
    @DisplayName("구매할 상품들 및 총값 조회")
    @WithMockUser(roles = "USER")
    void selectedForBuyCartList() throws Exception {
        // given
        List<Long> selectedCartIds = Arrays.asList(1L, 2L);

        CartSelectedRequestDto requestDto = CartSelectedRequestDto.builder()
                .cartIds(selectedCartIds)
                .build();

        CartItemResponseDto item1 = CartItemResponseDto.builder()
                .cartId(1L)
                .drinkId(2L)
                .drinkName("청주")
                .quantity(2)
                .unitPrice(5000)
                .totalPrice(10000)
                .imageUrl("http://example.com/image1.jpg")
                .build();

        CartItemResponseDto item2 = CartItemResponseDto.builder()
                .cartId(2L)
                .drinkId(3L)
                .drinkName("탁주")
                .quantity(1)
                .unitPrice(7000)
                .totalPrice(7000)
                .imageUrl("http://example.com/image2.jpg")
                .build();

        CartListResponseDto responseDto = CartListResponseDto.builder()
                .items(List.of(item1, item2))
                .totalPrice(17000)
                .deliveryCharge(3000)
                .totalPriceWithDelivery(20000)
                .build();

        when(cartService.getBuyCartList(any(), any())).thenReturn(responseDto);

        // when/then
        mockMvc.perform(post("/api/cart/buy")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].cartId").value(1))
                .andExpect(jsonPath("$.items[0].drinkName").value("청주"))
                .andExpect(jsonPath("$.items[1].cartId").value(2))
                .andExpect(jsonPath("$.items[1].drinkName").value("탁주"))
                .andExpect(jsonPath("$.totalPrice").value(17000))
                .andExpect(jsonPath("$.deliveryCharge").value(3000))
                .andExpect(jsonPath("$.totalPriceWithDelivery").value(20000));

        // verify
        verify(cartService).getBuyCartList(any(), any());
    }
}