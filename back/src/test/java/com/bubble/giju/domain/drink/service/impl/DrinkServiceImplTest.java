package com.bubble.giju.domain.drink.service.impl;

import com.bubble.giju.domain.category.dto.CategoryResponseDto;
import com.bubble.giju.domain.category.entity.Category;
import com.bubble.giju.domain.category.repository.CategoryRepository;
import com.bubble.giju.domain.drink.dto.DrinkDetailResponseDto;
import com.bubble.giju.domain.drink.dto.DrinkRequestDto;
import com.bubble.giju.domain.drink.dto.DrinkResponseDto;
import com.bubble.giju.domain.drink.dto.DrinkUpdateRequestDto;
import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.drink.entity.DrinkImage;
import com.bubble.giju.domain.drink.mapper.DrinkMapper;
import com.bubble.giju.domain.drink.repository.DrinkImageRepository;
import com.bubble.giju.domain.drink.repository.DrinkRepository;
import com.bubble.giju.domain.drink.service.DrinkService;
import com.bubble.giju.domain.image.entity.Image;
import com.bubble.giju.domain.image.repository.ImageRepository;
import com.bubble.giju.domain.image.service.ImageService;
import com.bubble.giju.domain.like.entity.Like;
import com.bubble.giju.domain.like.repository.LikeRepository;
import com.bubble.giju.domain.ranking.enums.Region;
import com.bubble.giju.domain.review.repository.ReviewRepository;
import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrinkServiceImplTest {

    @Mock
    private ImageService imageService;
    @Mock
    private DrinkRepository drinkRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private DrinkImageRepository drinkImageRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private DrinkMapper drinkMapper;

    @InjectMocks
    private DrinkServiceImpl drinkService;

    @BeforeEach
    void setUp() {
    }

    @DisplayName("상품(술) 등록 성공 테스트")
    @Test
    void saveDrink() throws IOException {
        // Given
        int categoryId = 1;
        String drinkName = "청하 13.5% 350mL";
        Category category = new Category("증류주");

        DrinkRequestDto requestDto = DrinkRequestDto.builder()
                .name("청하")
                .price(5000)
                .stock(100)
                .alcoholContent(13.5)
                .volume(350)
                .region("충청북도")
                .categoryId(categoryId)
                .build();

        MultipartFile file1 = new MockMultipartFile("file1", "image1.jpg", "image/jpeg", "dummy".getBytes());
        MultipartFile file2 = new MockMultipartFile("file2", "image2.jpg", "image/jpeg", "dummy".getBytes());
        MultipartFile thumbnail = new MockMultipartFile("thumb", "thumb.jpg", "image/jpeg", "thumb".getBytes());
        List<MultipartFile> files = List.of(file1, file2);

        // Mocked entities
        Drink savedDrink = Drink.builder()
                .id(1L)
                .name(drinkName)
                .category(category)
                .build();

        Image thumbnailImage = Image.builder().url("thumb.jpg").build();
        Image image1 = Image.builder().url("image1.jpg").build();
        Image image2 = Image.builder().url("image2.jpg").build();

        // Stubbing
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(drinkRepository.existsByName(anyString())).thenReturn(false);
        when(drinkRepository.save(any(Drink.class))).thenReturn(savedDrink);
        when(imageService.uploadFile(thumbnail)).thenReturn("thumb.jpg");
        when(imageRepository.findByUrl("thumb.jpg")).thenReturn(thumbnailImage);
        when(imageService.uploadFiles(files)).thenReturn(List.of("image1.jpg", "image2.jpg"));
        when(imageRepository.findAllByUrlIn(List.of("image1.jpg", "image2.jpg"))).thenReturn(List.of(image1, image2));
        when(drinkImageRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        DrinkResponseDto mockResponseDto = DrinkResponseDto.builder()
                .id(1L)
                .name(drinkName)
                .price(5000)
                .stock(100)
                .alcoholContent(13.5)
                .volume(350)
                .is_delete(false)
                .region("충청북도")
                .category(new CategoryResponseDto(1, "증류주"))
                .thumbnailUrl("thumb.jpg")
                .drinkImageUrlList(List.of("image1.jpg", "image2.jpg"))
                .build();

        // When
        DrinkResponseDto result = drinkService.saveDrink(requestDto, files, thumbnail);

        // Then
        assertNotNull(result);
        assertEquals(drinkName, result.getName());
        verify(drinkRepository).save(any(Drink.class));
        verify(drinkImageRepository).saveAll(anyList());
    }

    @DisplayName("상품(술) 삭제 성공테스트")
    @Test
    void deleteDrink() {
        // Given
        String drinkName = "청하 13.5% 350mL";
        Category category = new Category("증류주");

        Drink drink = Drink.builder()
                .id(1L)
                .name(drinkName)
                .price(5000)
                .stock(100)
                .alcoholContent(13.5)
                .volume(350)
                .region(Region.CHUNGBUK)
                .is_delete(false)
                .category(category)
                .build();

        // Mocking
        when(drinkRepository.findById(1L)).thenReturn(Optional.of(drink));
        when(drinkRepository.save(any(Drink.class))).thenReturn(drink);

        DrinkImage thumbnailImage = DrinkImage.builder()
                .drink(drink)
                .isThumbnail(true)
                .image(Image.builder().url("thumb.jpg").build())
                .build();

        when(drinkImageRepository.findByDrinkIdAndThumbnailIsTrue(1L))
                .thenReturn(thumbnailImage);
        when(drinkImageRepository.findByDrinkIdAndThumbnailIsFalse(1L))
                .thenReturn(List.of());  // 홍보 사진은 없음

        // Mapper stub
        when(drinkMapper.toDrinkResponseDto(any(Drink.class), anyString(), anyList()))
                .thenReturn(DrinkResponseDto.builder()
                        .id(drink.getId())
                        .name(drink.getName())
                        .price(drink.getPrice())
                        .stock(drink.getStock())
                        .alcoholContent(drink.getAlcoholContent())
                        .volume(drink.getVolume())
                        .is_delete(true)
                        .region(drink.getRegion().name())
                        .category(new CategoryResponseDto(category.getId(), category.getName()))
                        .thumbnailUrl("thumb.jpg")
                        .drinkImageUrlList(List.of())
                        .build());

        // When
        DrinkResponseDto result = drinkService.deleteDrink(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.is_delete());
        assertEquals("청하 13.5% 350mL", result.getName());

        verify(drinkRepository).findById(1L);
        verify(drinkRepository).save(any(Drink.class));
        verify(drinkMapper).toDrinkResponseDto(any(), anyString(), anyList());
    }

    @DisplayName("상품(술) 복구 테스트")
    @Test
    void restoreDrink() {
        // Given
        String drinkName = "청하 13.5% 350mL";
        Category category = new Category("증류주");

        Drink drink = Drink.builder()
                .id(1L)
                .name(drinkName)
                .price(5000)
                .stock(100)
                .alcoholContent(13.5)
                .volume(350)
                .region(Region.CHUNGBUK)
                .is_delete(false)
                .category(category)
                .build();

        // Mocking
        when(drinkRepository.findById(1L)).thenReturn(Optional.of(drink));
        when(drinkRepository.save(any(Drink.class))).thenReturn(drink);

        DrinkImage thumbnailImage = DrinkImage.builder()
                .drink(drink)
                .isThumbnail(true)
                .image(Image.builder().url("thumb.jpg").build())
                .build();

        when(drinkImageRepository.findByDrinkIdAndThumbnailIsTrue(1L))
                .thenReturn(thumbnailImage);
        when(drinkImageRepository.findByDrinkIdAndThumbnailIsFalse(1L))
                .thenReturn(List.of());  // 홍보 사진은 없음

        // Mapper stub
        when(drinkMapper.toDrinkResponseDto(any(Drink.class), anyString(), anyList()))
                .thenReturn(DrinkResponseDto.builder()
                        .id(drink.getId())
                        .name(drink.getName())
                        .price(drink.getPrice())
                        .stock(drink.getStock())
                        .alcoholContent(drink.getAlcoholContent())
                        .volume(drink.getVolume())
                        .is_delete(false)
                        .region(drink.getRegion().name())
                        .category(new CategoryResponseDto(category.getId(), category.getName()))
                        .thumbnailUrl("thumb.jpg")
                        .drinkImageUrlList(List.of())
                        .build());

        // When
        DrinkResponseDto result = drinkService.restoreDrink(1L);

        // Then
        assertNotNull(result);
        assertFalse(result.is_delete());
        assertEquals("청하 13.5% 350mL", result.getName());

        verify(drinkRepository).findById(1L);
        verify(drinkRepository).save(any(Drink.class));
        verify(drinkMapper).toDrinkResponseDto(any(), anyString(), anyList());
    }

    @DisplayName("상품(술) 업데이트 성공 테스트")
    @Test
    void updateDrink() {

        // given
        Long drinkId = 1L;
        Category category=new Category("증류주");
        int categoryId = 1;
        ReflectionTestUtils.setField(category, "id", categoryId); // private 필드 설정
        Drink drink = Drink.builder().id(drinkId).build();
        DrinkUpdateRequestDto drinkUpdateRequestDto = DrinkUpdateRequestDto.builder().price(50000).stock(100).region("충청북도").categoryId(1).build();

        when(drinkRepository.findById(drinkId)).thenReturn(Optional.of(drink));
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(drinkRepository.save(any(Drink.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DrinkResponseDto responseDto = DrinkResponseDto.builder()
                .id(drinkId)
                .name("청하 13.5% 350mL")
                .price(50000)
                .stock(200) // 테스트에서 100이 아니라 200으로 설정했네요!
                .alcoholContent(13.5)
                .volume(350)
                .is_delete(false)
                .region("충청북도")
                .category(new CategoryResponseDto(categoryId, category.getName()))
                .thumbnailUrl("thumb.jpg")
                .drinkImageUrlList(List.of("img1.jpg", "img2.jpg"))
                .build();

        when(drinkMapper.toDrinkResponseDto(any(), any(), any())).thenReturn(responseDto);
        when(drinkImageRepository.findByDrinkIdAndThumbnailIsTrue(drinkId)).thenReturn(null);
        when(drinkImageRepository.findByDrinkIdAndThumbnailIsFalse(drinkId)).thenReturn(Collections.emptyList());

        // when
        DrinkResponseDto result = drinkService.updateDrink(drinkId, drinkUpdateRequestDto);
        assertNotNull(result);
        assertEquals(50000, result.getPrice());
        assertEquals(200, result.getStock());
        assertEquals("충청북도", result.getRegion());
        assertEquals(categoryId, result.getCategory().getId());


    }

    @DisplayName("상품(술) 단일 조회 성공테스트")
    @Test
    void findById() {
        // given
        Long drinkId=1L;
        Drink drink = Drink.builder().id(drinkId).is_delete(false).build();

        int categoryId = 1;
        Category category = new Category("증류주");
        ReflectionTestUtils.setField(category, "id", categoryId);

        DrinkResponseDto responseDto = DrinkResponseDto.builder()
                .id(drinkId)
                .name("청하 13.5% 350mL")
                .price(50000)
                .stock(200) // 테스트에서 100이 아니라 200으로 설정했네요!
                .alcoholContent(13.5)
                .volume(350)
                .is_delete(false)
                .region("충청북도")
                .category(new CategoryResponseDto(categoryId, category.getName()))
                .thumbnailUrl("thumb.jpg")
                .drinkImageUrlList(List.of("img1.jpg", "img2.jpg"))
                .build();

        long reviewSum = 3;
        long reviewCount= 3;

        double reviewScore = (double) reviewSum / reviewCount;

        UUID userId= UUID.randomUUID();
        User user= User.builder().build();
        Like like = Like.builder().id(1L).drink(drink).user(user).delete(false).createdAt(null).build();

        when(drinkRepository.findById(drinkId)).thenReturn(Optional.of(drink));
        when(reviewRepository.findSumScoreByDrinkId(drinkId)).thenReturn(reviewSum);
        when(reviewRepository.countByDrinkId(drinkId)).thenReturn(reviewCount);
        when(likeRepository.findByUser_UserIdAndDrink_Id(userId, drinkId)).thenReturn(Optional.of(like));
        when(drinkImageRepository.findByDrinkIdAndThumbnailIsTrue(drinkId)).thenReturn(
                DrinkImage.builder()
                        .drink(drink)
                        .isThumbnail(true)
                        .image(Image.builder().url("thumb.jpg").build())
                        .build()
        );
        when(drinkImageRepository.findByDrinkIdAndThumbnailIsFalse(drinkId)).thenReturn(
                List.of(
                        DrinkImage.builder()
                                .drink(drink)
                                .isThumbnail(false)
                                .image(Image.builder().url("img1.jpg").build())
                                .build(),
                        DrinkImage.builder()
                                .drink(drink)
                                .isThumbnail(false)
                                .image(Image.builder().url("img2.jpg").build())
                                .build()
                )
        );

        DrinkDetailResponseDto drinkDetailResponseDto = drinkService.findById(drinkId,userId);

        when(drinkMapper.toDrinkResponseDto(any(), any(), any())).thenReturn(responseDto);
        when(drinkMapper.toDrinkDetailResponseDto(eq(responseDto), eq(reviewScore), eq(reviewCount), eq(true)))
                .thenReturn(
                        DrinkDetailResponseDto.builder()
                                .id(responseDto.getId())
                                .name(responseDto.getName())
                                .price(responseDto.getPrice())
                                .stock(responseDto.getStock())
                                .alcoholContent(responseDto.getAlcoholContent())
                                .volume(responseDto.getVolume())
                                .is_delete(responseDto.is_delete())
                                .region(responseDto.getRegion())
                                .category(responseDto.getCategory())
                                .thumbnailUrl(responseDto.getThumbnailUrl())
                                .drinkImageUrlList(responseDto.getDrinkImageUrlList())
                                .reviewCount(reviewCount)
                                .reviewScore(reviewScore)
                                .is_like(true)
                                .build()
                );

        // When
        DrinkDetailResponseDto result = drinkService.findById(drinkId, userId);

        // Then
        assertNotNull(result);
        assertEquals("청하 13.5% 350mL", result.getName());
        assertEquals(50000, result.getPrice());
        assertEquals(200, result.getStock());
        assertEquals(13.5, result.getAlcoholContent());
        assertEquals(350, result.getVolume());
        assertEquals("충청북도", result.getRegion());
        assertEquals("thumb.jpg", result.getThumbnailUrl());
        assertEquals(List.of("img1.jpg", "img2.jpg"), result.getDrinkImageUrlList());
        assertEquals(reviewScore, result.getReviewScore());
        assertEquals(reviewCount, result.getReviewCount());
        assertTrue(result.is_like());

    }

    @DisplayName("카테고리 검색 시 Drink 목록 반환 테스트")
    @Test
    void findDrinksByCategory() {
        // given
        String type = "category";
        String keyword = "1"; // 카테고리 ID
        UUID userUuid = UUID.randomUUID();
        int pageNum = 0;

        Category category = new Category("탁주");
        ReflectionTestUtils.setField(category, "id", 1);

        Drink drink = Drink.builder()
                .id(1L)
                .name("막걸리")
                .region(Region.CHUNGBUK)
                .category(category)
                .is_delete(false)
                .build();

        Page<Drink> drinkPage = new PageImpl<>(List.of(drink));

        // Mocking repository
        when(drinkRepository.findByCategoryIdAndDeletedFalse(1, PageRequest.of(pageNum, 6)))
                .thenReturn(drinkPage);
        when(reviewRepository.findSumScoreByDrinkId(1L)).thenReturn(5L);
        when(reviewRepository.countByDrinkId(1L)).thenReturn(2L);
        when(likeRepository.findByUser_UserIdAndDrink_Id(userUuid, 1L)).thenReturn(Optional.empty());
        when(drinkImageRepository.findByDrinkIdAndThumbnailIsTrue(1L)).thenReturn(null);
        when(drinkImageRepository.findByDrinkIdAndThumbnailIsFalse(1L)).thenReturn(Collections.emptyList());

        // Mocking mapper
        when(drinkMapper.toDrinkResponseDto(any(Drink.class), any(), any()))
                .thenReturn(DrinkResponseDto.builder()
                        .id(1L)
                        .name("막걸리")
                        .price(10000)
                        .stock(50)
                        .alcoholContent(6.0)
                        .volume(750)
                        .is_delete(false)
                        .region("충청북도")
                        .category(new CategoryResponseDto(1, "탁주"))
                        .thumbnailUrl(null)
                        .drinkImageUrlList(Collections.emptyList())
                        .build());

        when(drinkMapper.toDrinkDetailResponseDto(any(DrinkResponseDto.class), eq(2.5), eq(2L), eq(false)))
                .thenReturn(DrinkDetailResponseDto.builder()
                        .id(1L)
                        .name("막걸리")
                        .price(10000)
                        .stock(50)
                        .alcoholContent(6.0)
                        .volume(750)
                        .is_delete(false)
                        .region("충청북도")
                        .category(new CategoryResponseDto(1, "탁주"))
                        .thumbnailUrl(null)
                        .drinkImageUrlList(Collections.emptyList())
                        .reviewScore(2.5)
                        .reviewCount(2L)
                        .is_like(false)
                        .build());

        // when
        Page<DrinkDetailResponseDto> result = drinkService.findDrinks(type, keyword, pageNum, userUuid);

        // then
        assertEquals(1, result.getTotalElements());

        DrinkDetailResponseDto dto = result.getContent().get(0);
        assertEquals("막걸리", dto.getName());
        assertEquals(2L, dto.getReviewCount());
        assertEquals(2.5, dto.getReviewScore());
        assertFalse(dto.is_like());
    }


}