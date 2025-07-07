package com.bubble.giju.domain.like.dto;

import com.bubble.giju.domain.like.entity.Like;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class LikeDto {

    @AllArgsConstructor
    @Builder
    @Getter
    public static class LikeResponse {
        String userId;
        Long drinkId;
        LocalDateTime createdAt;

        public static LikeResponse fromEntity(Like like) {
            return new LikeResponse(
                    like.getUser().getUserId().toString(),
                    like.getDrink().getId(),
                    like.getCreatedAt()
            );
        }
    }
}
