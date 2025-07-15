package com.bubble.giju.domain.drink.repository;

import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.ranking.enums.Region;
import com.querydsl.core.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DrinkRepositoryCustom {

    Page<Tuple> findDrinksCustom(String type,String keyword,UUID userId, Pageable pageable);
}
