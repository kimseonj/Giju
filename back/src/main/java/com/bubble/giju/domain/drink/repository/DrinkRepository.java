package com.bubble.giju.domain.drink.repository;

import com.bubble.giju.domain.drink.entity.Drink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DrinkRepository extends JpaRepository<Drink, Long> {
    @Query("SELECT d FROM Drink d WHERE d.category.id = :category_id AND d.is_delete = false")
    Page<Drink> findByCategoryIdIAndIs_deleteFalse(int category_id, Pageable pageable);
    @Query("SELECT d FROM Drink d WHERE d.region = :region AND d.is_delete = false")
    Page<Drink> findByRegionIsDeleteFalse(String region, Pageable pageable);
    @Query("SELECT d FROM Drink d WHERE d.name LIKE %:name% AND d.is_delete = false")
    Page<Drink> findByNameContainsIAndIs_deleteFalse(String name, Pageable pageable);
}
