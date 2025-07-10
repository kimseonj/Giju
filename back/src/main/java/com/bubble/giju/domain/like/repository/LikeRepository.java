package com.bubble.giju.domain.like.repository;

import com.bubble.giju.domain.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUser_UserIdAndDrink_id(UUID userId, Long drinkId);
    Optional<Like> findByUser_UserIdAndId(UUID userUserId, Long id);
    List<Like> findByUser_UserIdAndDeleteFalseOrderByCreatedAtDesc(UUID uuid);
    Optional<Like> findByUser_UserIdAndDrink_Id(UUID userUserId, Long drinkId);
}
