package com.bubble.giju.domain.review.dto;

import com.bubble.giju.domain.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ReviewDto {

    @Getter
    public static class Request {
        String content;
        int score;
    }

    @AllArgsConstructor
    @Getter
    public static class Response {
        String userName;
        String content;
        int score;

        public static Response fromEntity(Review review) {
            Response response = new Response(
                    review.getUser().getName(),
                    review.getContent(),
                    review.getScore()
            );

            return response;
        }
    }
}
