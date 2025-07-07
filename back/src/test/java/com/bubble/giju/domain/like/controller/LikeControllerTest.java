package com.bubble.giju.domain.like.controller;

import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.like.dto.LikeDto;
import com.bubble.giju.domain.like.entity.Like;
import com.bubble.giju.domain.like.service.LikeService;
import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.domain.user.enums.Role;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import com.bubble.giju.global.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LikeController.class,
        excludeAutoConfiguration = {SecurityConfig.class})
class LikeControllerTest {
    @MockitoBean
    private LikeService likeService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("찜하기 성공 - 정상 사용자와 음료 ID로 찜하기")
    void addLike_성공테스트() throws Exception {
        // given
        String userId = UUID.randomUUID().toString();
        Long drinkId = 1L;

        // Mock Response (Entity 생성 없이 DTO만 생성)
        LikeDto.LikeResponse mockResponse = LikeDto.LikeResponse.builder()
                .userId(userId)
                .drinkId(drinkId)
                .createdAt(LocalDateTime.now())
                .build();

        given(likeService.setLike(any(String.class), anyLong(), any(Boolean.class)))
                .willReturn(mockResponse);

        // CustomPrincipal 생성
        CustomPrincipal customPrincipal = createCustomPrincipal(userId);

        //when & then
        mockMvc.perform(post("/api/drinks/{drinkId}/wishlist", drinkId)
                        .with(user(customPrincipal))
                        .with(csrf()) // CSRF 처리 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("찜하기 성공 완료"))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.drinkId").value(drinkId));
    }

    @Test
    @DisplayName("찜하기 취소 성공 - 정상 사용자와 음료 ID로 찜하기 취소")
    void deleteLike_성공() throws Exception {
        // given
        String userId = UUID.randomUUID().toString();
        Long drinkId = 1L;

        // Mock Response (Entity 생성 없이 DTO만 생성)
        LikeDto.LikeResponse mockResponse = LikeDto.LikeResponse.builder()
                .userId(userId)
                .drinkId(drinkId)
                .createdAt(LocalDateTime.now())
                .build();

        given(likeService.setLike(any(String.class), anyLong(), any(Boolean.class)))
                .willReturn(mockResponse);

        // CustomPrincipal 생성
        CustomPrincipal customPrincipal = createCustomPrincipal(userId);

        // when & then
        mockMvc.perform(delete("/api/drinks/{drinkId}/wishlist", drinkId)
                        .with(user(customPrincipal))
                        .with(csrf()) // CSRF 처리 추가
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("찜하기 취소 완료"))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.drinkId").value(drinkId));
    }

    @Test
    @DisplayName("찜하기 실패 - userId 없음")
    void addLike_실패() throws Exception {
        // given
        String userId = UUID.randomUUID().toString();
        Long drinkId = 1L;

        // CustomPrincipal 생성
        CustomPrincipal customPrincipal = createCustomPrincipal(userId);

        // mockResponse
        given(likeService.setLike(any(String.class), anyLong(), any(Boolean.class)))
                .willThrow(new CustomException(ErrorCode.NON_EXISTENT_USER));

        // when & then
        mockMvc.perform(post("/api/drinks/{drinkId}/wishlist", drinkId)
                        .with(user(customPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("존재하지 않는 유저입니다"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("찜하기 실패 - 존재하지 않는 drinkId")
    void addLike_잘못된_drinkId() throws Exception {
        // given
        String userId = UUID.randomUUID().toString();
        Long invalidDrinkId = 9999L;

        // CustomPrincipal 생성
        CustomPrincipal customPrincipal = createCustomPrincipal(userId);

        given(likeService.setLike(any(String.class), anyLong(), any(Boolean.class)))
                .willThrow(new CustomException(ErrorCode.NON_EXISTENT_DRINK));

        // when & then
        mockMvc.perform(post("/api/drinks/{drinkId}/wishlist", invalidDrinkId)
                        .with(user(customPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 술은 존재 하지않음"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("찜하기 실패 - 잘못된 like 요청")
    void addLike_잘못된_like요청() throws Exception {
        // given
        String userId = UUID.randomUUID().toString();
        Long invalidDrinkId = 1L;

        // CustomPrincipal 생성
        CustomPrincipal customPrincipal = createCustomPrincipal(userId);

        given(likeService.setLike(any(String.class), anyLong(), any(Boolean.class)))
                .willThrow(new CustomException(ErrorCode.INVALID_LIKE));

        // when & then
        mockMvc.perform(post("/api/drinks/{drinkId}/wishlist", invalidDrinkId)
                        .with(user(customPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 찜 요청입니다."))
                .andExpect(jsonPath("$.status").value(400));
    }

    private CustomPrincipal createCustomPrincipal(String userId) {
        User user = User.builder()
                .userId(UUID.fromString(userId))
                .loginId("testUser")
                .name("testName")
                .password("testPassword")
                .role(Role.USER)
                .build();
        return new CustomPrincipal(user);
    }
}