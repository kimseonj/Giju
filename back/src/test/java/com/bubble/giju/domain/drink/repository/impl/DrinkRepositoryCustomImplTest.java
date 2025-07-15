package com.bubble.giju.domain.drink.repository.impl;

import com.bubble.giju.domain.category.entity.Category;
import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.drink.entity.QDrink;
import com.bubble.giju.domain.ranking.enums.Region;
import com.querydsl.core.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import jakarta.persistence.EntityManager;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DrinkRepositoryCustomImplTest {

    @Autowired
    private EntityManager em;

    @InjectMocks
    private DrinkRepositoryCustomImpl drinkRepositoryCustom;

    @Test
    @DisplayName("findDrinksCustom 단위 테스트 - 카테고리 검색")
    void findDrinksCustomByCategory() {
        drinkRepositoryCustom.setEntityManager(em);  // QuerydslRepositorySupport는 엔티티매니저 주입 필요

        // given - 테스트용 데이터 삽입 (TestEntityManager 없어도 em으로 삽입 가능)
        Category category = new Category("탁주");
        em.persist(category);

        Drink drink = Drink.builder()
                .name("막걸리")
                .price(10000)
                .stock(50)
                .alcoholContent(6.0)
                .volume(750)
                .is_delete(false)
                .region(Region.충청북도)
                .category(category)
                .build();
        em.persist(drink);

        em.flush();
        em.clear();

        // when
        Page<Tuple> page = drinkRepositoryCustom.findDrinksCustom("category", String.valueOf(category.getId()), UUID.randomUUID(),
                PageRequest.of(0, 10));

        // then
        assertThat(page.getTotalElements()).isGreaterThan(0);
        Tuple tuple = page.getContent().get(0);
        assertThat(tuple.get(QDrink.drink.name)).isEqualTo("막걸리");
        assertThat(tuple.get(QDrink.drink.region)).isEqualTo(Region.충청북도);
    }
}
