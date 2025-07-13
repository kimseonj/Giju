package com.bubble.giju.domain.drink.mapper;

import com.bubble.giju.domain.category.dto.CategoryResponseDto;
import com.bubble.giju.domain.category.entity.Category;
import com.bubble.giju.domain.drink.dto.DrinkDetailResponseDto;
import com.bubble.giju.domain.drink.dto.DrinkResponseDto;
import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.ranking.enums.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class DrinkMapperTest {
    private DrinkMapper drinkMapper;

    @BeforeEach
    void setUp() {
        drinkMapper = new DrinkMapper();
    }

    @Test
    void toDrinkResponseDto_매핑_성공() {
        // given
        Category category = new Category("탁주");
        // 테스트에서는 ID를 강제로 설정 (엔티티 직접 생성 시에는 setter가 없을 수도 있어 Reflection 사용 가능)
        ReflectionTestUtils.setField(category, "id", 1);

        Drink drink = Drink.builder()
                .id(100L)
                .name("막걸리")
                .price(3000)
                .stock(50)
                .alcoholContent(6.5)
                .volume(750)
                .is_delete(false)
                .region(Region.강원도)
                .category(category)
                .build();

        String thumbnailUrl = "https://test.com/thumb.jpg";
        List<String> imageUrls = List.of("https://test.com/img1.jpg", "https://test.com/img2.jpg");

        // when
        DrinkResponseDto result = drinkMapper.toDrinkResponseDto(drink, thumbnailUrl, imageUrls);

        // then
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getName()).isEqualTo("막걸리");
        assertThat(result.getPrice()).isEqualTo(3000);
        assertThat(result.getStock()).isEqualTo(50);
        assertThat(result.getAlcoholContent()).isEqualTo(6.5);
        assertThat(result.getVolume()).isEqualTo(750);
        assertThat(result.is_delete()).isFalse();
        assertThat(result.getRegion()).isEqualTo("강원도");
        assertThat(result.getCategory().getId()).isEqualTo(1);
        assertThat(result.getCategory().getName()).isEqualTo("탁주");
        assertThat(result.getThumbnailUrl()).isEqualTo(thumbnailUrl);
        assertThat(result.getDrinkImageUrlList()).isEqualTo(imageUrls);
    }

    @Test
    void toDrinkDetailResponseDto_매핑_성공() {
        // given
        DrinkResponseDto responseDto = DrinkResponseDto.builder()
                .id(100L)
                .name("막걸리")
                .price(3000)
                .stock(50)
                .alcoholContent(6.5)
                .volume(750)
                .is_delete(false)
                .region("서울")
                .category(new CategoryResponseDto(1, "탁주"))
                .thumbnailUrl("https://test.com/thumb.jpg")
                .drinkImageUrlList(List.of("https://test.com/img1.jpg"))
                .build();

        double reviewScore = 4.5;
        long reviewCount = 20;
        boolean isLike = true;

        // when
        DrinkDetailResponseDto result = drinkMapper.toDrinkDetailResponseDto(responseDto, reviewScore, reviewCount, isLike);

        // then
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getName()).isEqualTo("막걸리");
        assertThat(result.getPrice()).isEqualTo(3000);
        assertThat(result.getStock()).isEqualTo(50);
        assertThat(result.getAlcoholContent()).isEqualTo(6.5);
        assertThat(result.getVolume()).isEqualTo(750);
        assertThat(result.is_delete()).isFalse();
        assertThat(result.getRegion()).isEqualTo("서울");
        assertThat(result.getCategory().getId()).isEqualTo(1);
        assertThat(result.getCategory().getName()).isEqualTo("탁주");
        assertThat(result.getThumbnailUrl()).isEqualTo("https://test.com/thumb.jpg");
        assertThat(result.getReviewScore()).isEqualTo(4.5);
        assertThat(result.getReviewCount()).isEqualTo(20);
        assertThat(result.is_like()).isTrue();
    }
}