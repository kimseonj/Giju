package com.bubble.giju.domain.review.repository;

import com.bubble.giju.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT COALESCE(SUM(r.score), 0) FROM Review r WHERE r.drink.id = :drinkId")
    long findSumScoreByDrinkId(Long drinkId);
    long countByDrinkId(Long drinkId);

    List<Review> findAllByDrink_Id(Long drinkId);

    List<Review> findAllByUser_UserId(UUID userUserId);
}
