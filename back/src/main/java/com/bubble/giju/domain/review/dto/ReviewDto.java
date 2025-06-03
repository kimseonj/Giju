package com.bubble.giju.domain.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ReviewDto {

    @Getter
    public static class Request {
        String content;
        int score;
    }
}
