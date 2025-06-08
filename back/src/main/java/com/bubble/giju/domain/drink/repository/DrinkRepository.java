package com.bubble.giju.domain.drink.repository;

import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.ranking.enums.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DrinkRepository extends JpaRepository<Drink, Long> {
    Page<Drink> findByCategoryIdAndDeletedFalse(int category_id, Pageable pageable);
    Page<Drink> findByRegionAndDeletedFalse(Region region, Pageable pageable);
    Page<Drink> findByNameContainsAndDeletedFalse(String name, Pageable pageable);
    Page<Drink> findByDeletedFalseOrderByNameAsc(Pageable pageable);

    boolean existsByName(String name);
    Optional<Drink> findByName(String name);
}
