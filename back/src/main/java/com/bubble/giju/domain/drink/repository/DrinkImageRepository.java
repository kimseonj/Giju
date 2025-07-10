package com.bubble.giju.domain.drink.repository;

import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.drink.entity.DrinkImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrinkImageRepository extends JpaRepository<DrinkImage, Long> {
    DrinkImage findByDrinkIdAndThumbnailIsTrue(Long drinkId);
    List<DrinkImage> findByDrinkIdAndThumbnailIsFalse(Long drinkId);
    Optional<DrinkImage> findFirstByDrinkAndThumbnailTrue(Drink drink);

}
