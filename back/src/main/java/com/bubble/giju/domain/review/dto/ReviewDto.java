package com.bubble.giju.domain.review.dto;

import com.bubble.giju.domain.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ReviewDto {

    @Getter
    public static class ReviewRequest {
        String content;
        int score;
    }

    @AllArgsConstructor
    @Getter
    public static class ReviewResponse {
        String userName;
        String content;
        int score;

        public static ReviewResponse fromEntity(Review review) {
            ReviewResponse response = new ReviewResponse(
                    review.getUser().getName(),
                    review.getContent(),
                    review.getScore()
            );

            return response;
        }
    }
}
