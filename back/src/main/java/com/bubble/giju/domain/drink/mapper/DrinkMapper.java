package com.bubble.giju.domain.drink.mapper;

import com.bubble.giju.domain.category.dto.CategoryResponseDto;
import com.bubble.giju.domain.drink.dto.DrinkDetailResponseDto;
import com.bubble.giju.domain.drink.dto.DrinkResponseDto;
import com.bubble.giju.domain.drink.entity.Drink;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DrinkMapper {

    public DrinkResponseDto toDrinkResponseDto(Drink drink, String thumbnailUrl, List<String> drinkImageUrlList) {
        return DrinkResponseDto.builder()
                .id(drink.getId())
                .name(drink.getName())
                .price(drink.getPrice())
                .stock(drink.getStock())
                .alcoholContent(drink.getAlcoholContent())
                .volume(drink.getVolume())
                .is_delete(drink.is_delete())
                .region(String.valueOf(drink.getRegion()))
                .category(new CategoryResponseDto(drink.getCategory().getId(), drink.getCategory().getName()))
                .thumbnailUrl(thumbnailUrl)
                .drinkImageUrlList(drinkImageUrlList)
                .build();
    }

    public DrinkDetailResponseDto toDrinkDetailResponseDto(DrinkResponseDto dto, double reviewScore, long reviewCount, boolean isLike) {
        return DrinkDetailResponseDto.builder()
                .id(dto.getId())
                .name(dto.getName())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .alcoholContent(dto.getAlcoholContent())
                .volume(dto.getVolume())
                .is_delete(dto.is_delete())
                .region(dto.getRegion())
                .category(dto.getCategory())
                .thumbnailUrl(dto.getThumbnailUrl())
                .drinkImageUrlList(dto.getDrinkImageUrlList())
                .reviewScore(reviewScore)
                .reviewCount(reviewCount)
                .is_like(isLike)
                .build();
    }
}

