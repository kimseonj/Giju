package com.bubble.giju.domain.like.service.impl;

import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.drink.repository.DrinkRepository;
import com.bubble.giju.domain.like.dto.LikeDto;
import com.bubble.giju.domain.like.entity.Like;
import com.bubble.giju.domain.like.repository.LikeRepository;
import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.domain.user.repository.UserRepository;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {
    @InjectMocks
    private LikeServiceImpl likeService;

    @Mock
    private DrinkRepository drinkRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LikeRepository likeRepository;

    private String testUserId;
    private UUID testUUID;
    private User testUser;
    private Drink testDrink;
    private Long testDrinkId;
    private Like testLike;

    @BeforeEach
    void setUp() {
        testUUID = UUID.randomUUID();
        testUserId = testUUID.toString();
        testDrinkId = 1L;

        // 실제 객체 생성 (mock 대신)
        testUser = User.builder()
                .userId(testUUID)
                .build();

        testDrink = Drink.builder()
                .id(testDrinkId)
                .build();

        testLike = Like.builder()
                .user(testUser)
                .drink(testDrink)
                .delete(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("좋아요 목록 조회 - 성공")
    void getLike_success() {
        // given
        given(userRepository.findById(testUUID))
                .willReturn(Optional.of(testUser));
        given(likeRepository.findByUser_UserIdAndDeleteFalseOrderByCreatedAtDesc(testUUID))
                .willReturn(List.of(testLike));

        // when
        List<LikeDto.LikeResponse> result = likeService.getLike(testUserId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findById(testUUID);
        verify(likeRepository).findByUser_UserIdAndDeleteFalseOrderByCreatedAtDesc(testUUID);
    }

    @Test
    @DisplayName("좋아요 목록 조회 - 실패 - 존재하지 않는 사용자")
    void getLike_UserNotFound() {
        // given
        when(userRepository.findById(testUUID))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () ->
                likeService.getLike(testUserId));

        assertEquals(ErrorCode.NON_EXISTENT_USER, exception.getErrorCode());
        verify(userRepository).findById(testUUID);
        verify(likeRepository, never()).findByUser_UserIdAndDeleteFalseOrderByCreatedAtDesc(testUUID);
    }

    @Test
    @DisplayName("좋아요 성공 테스트 - 좋아요 처음 활성화")
    void setLike_addLike() {
        // given
        boolean likeRequest = true;
        given(userRepository.findById(testUUID)).willReturn(Optional.of(testUser));
        given(drinkRepository.findById(testDrinkId)).willReturn(Optional.of(testDrink));
        given(likeRepository.findByUser_UserIdAndDrink_Id(testUUID, testDrinkId))
                .willReturn(Optional.empty()); // 첫 번째 좋아요이므로 기존 데이터 없음
        given(likeRepository.save(any(Like.class))).willReturn(testLike);

        // when
        LikeDto.LikeResponse likeResponse = likeService.setLike(testUserId, testDrinkId, likeRequest);

        // then
        assertNotNull(likeResponse);
        verify(userRepository).findById(testUUID);
        verify(drinkRepository).findById(testDrinkId);
        verify(likeRepository).findByUser_UserIdAndDrink_Id(testUUID, testDrinkId);
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    @DisplayName("좋아요 재활성화 테스트 - 삭제된 좋아요 다시 활성화")
    void setLike_addLike2() {
        // given
        boolean likeRequest = true;
        Like deletedLike = Like.builder()
                .user(testUser)
                .drink(testDrink)
                .delete(true) // 삭제 상태
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(testUUID)).willReturn(Optional.of(testUser));
        given(drinkRepository.findById(testDrinkId)).willReturn(Optional.of(testDrink));
        given(likeRepository.findByUser_UserIdAndDrink_Id(testUUID, testDrinkId))
                .willReturn(Optional.of(deletedLike));

        // when
        LikeDto.LikeResponse result = likeService.setLike(testUserId, testDrinkId, likeRequest);

        // then
        assertNotNull(result);
        assertFalse(deletedLike.isDelete()); // 삭제 상태가 해제되었는지 확인
        verify(userRepository).findById(testUUID);
        verify(drinkRepository).findById(testDrinkId);
        verify(likeRepository).findByUser_UserIdAndDrink_Id(testUUID, testDrinkId);
        verify(likeRepository, never()).save(any(Like.class)); // 새로 저장하지 않음
        assertFalse(deletedLike.isDelete());
    }

    @Test
    @DisplayName("좋아요 취소 - 성공")
    void setLike_deleteLike() {
        // given
        boolean likeRequest = false;
        Like activeLike = Like.builder()
                .user(testUser)
                .drink(testDrink)
                .delete(false) // 활성 상태
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(testUUID)).willReturn(Optional.of(testUser));
        given(drinkRepository.findById(testDrinkId)).willReturn(Optional.of(testDrink));
        given(likeRepository.findByUser_UserIdAndDrink_Id(testUUID, testDrinkId))
                .willReturn(Optional.of(activeLike));

        // when
        LikeDto.LikeResponse result = likeService.setLike(testUserId, testDrinkId, likeRequest);

        // then
        assertNotNull(result);
        assertTrue(activeLike.isDelete());
        verify(userRepository).findById(testUUID);
        verify(drinkRepository).findById(testDrinkId);
        verify(likeRepository).findByUser_UserIdAndDrink_Id(testUUID, testDrinkId);
    }

    @Test
    @DisplayName("좋아요 테스트 - 실패 - 이미 활성화된 좋아요")
    void setLike_addLike_실패() {
        // given
        // given
        boolean likeRequest = true;
        Like activeLike = Like.builder()
                .user(testUser)
                .drink(testDrink)
                .delete(false) // 이미 활성 상태
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(testUUID)).willReturn(Optional.of(testUser));
        given(drinkRepository.findById(testDrinkId)).willReturn(Optional.of(testDrink));
        given(likeRepository.findByUser_UserIdAndDrink_Id(testUUID, testDrinkId))
                .willReturn(Optional.of(activeLike));

        // when
        CustomException customException = assertThrows(CustomException.class, () ->
                likeService.setLike(testUserId, testDrinkId, likeRequest));

        // then
        assertNotNull(customException);
        assertEquals(ErrorCode.INVALID_LIKE, customException.getErrorCode());
        verify(userRepository).findById(testUUID);
        verify(drinkRepository).findById(testDrinkId);
        verify(likeRepository).findByUser_UserIdAndDrink_Id(testUUID, testDrinkId);
    }

    @Test
    @DisplayName("좋아요 테스트 - 실패 - 존재하지 않는 좋아요")
    void setLike_deleteLike_실패() {
        // given
        boolean likeRequest = false;
        given(userRepository.findById(testUUID)).willReturn(Optional.of(testUser));
        given(drinkRepository.findById(testDrinkId)).willReturn(Optional.of(testDrink));
        given(likeRepository.findByUser_UserIdAndDrink_Id(testUUID, testDrinkId))
                .willReturn(Optional.empty());

        // when
        CustomException customException = assertThrows(CustomException.class, () ->
                likeService.setLike(testUserId, testDrinkId, likeRequest));

        // then
        assertNotNull(customException);
        assertEquals(ErrorCode.INVALID_LIKE, customException.getErrorCode());
        verify(userRepository).findById(testUUID);
        verify(drinkRepository).findById(testDrinkId);
        verify(likeRepository).findByUser_UserIdAndDrink_Id(testUUID, testDrinkId);
    }

    @Test
    @DisplayName("좋아요 삭제 - 실패 - 이미 삭제된 좋아요")
    void setLike_DeleteLike_AlreadyDeleted_Fail() {
        // given
        boolean likeRequest = false;
        Like deletedLike = Like.builder()
                .user(testUser)
                .drink(testDrink)
                .delete(true) // 이미 삭제 상태
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(testUUID)).willReturn(Optional.of(testUser));
        given(drinkRepository.findById(testDrinkId)).willReturn(Optional.of(testDrink));
        given(likeRepository.findByUser_UserIdAndDrink_Id(testUUID, testDrinkId))
                .willReturn(Optional.of(deletedLike));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () ->
                likeService.setLike(testUserId, testDrinkId, likeRequest));

        assertEquals(ErrorCode.INVALID_LIKE, exception.getErrorCode());
        verify(userRepository).findById(testUUID);
        verify(drinkRepository).findById(testDrinkId);
        verify(likeRepository).findByUser_UserIdAndDrink_Id(testUUID, testDrinkId);
    }

    @Test
    @DisplayName("좋아요 설정 실패 - 존재하지 않는 사용자")
    void setLike_UserNotFound_Fail() {
        // given
        boolean likeRequest = true;
        given(userRepository.findById(testUUID)).willReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () ->
                likeService.setLike(testUserId, testDrinkId, likeRequest));

        assertEquals(ErrorCode.NON_EXISTENT_USER, exception.getErrorCode());
        verify(userRepository).findById(testUUID);
        verify(drinkRepository, never()).findById(testDrinkId);
        verify(likeRepository, never()).findByUser_UserIdAndDrink_Id(testUUID, testDrinkId);
    }

    @Test
    @DisplayName("좋아요 설정 실패 - 존재하지 않는 음료")
    void setLike_DrinkNotFound_Fail() {
        // given
        boolean likeRequest = true;
        given(userRepository.findById(testUUID)).willReturn(Optional.of(testUser));
        given(drinkRepository.findById(testDrinkId)).willReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () ->
                likeService.setLike(testUserId, testDrinkId, likeRequest));

        assertEquals(ErrorCode.NON_EXISTENT_DRINK, exception.getErrorCode());
        verify(userRepository).findById(testUUID);
        verify(drinkRepository).findById(testDrinkId);
        verify(likeRepository, never()).findByUser_UserIdAndDrink_Id(testUUID, testDrinkId);
    }
}