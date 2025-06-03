package com.bubble.giju.domain.review.service;

import com.bubble.giju.domain.review.dto.ReviewDto;

public interface ReviewService {
    void create(String userId, Long orderId, Long drinkId, ReviewDto.Request reviewRequest);
    void getReviewsByDrinkId(Long drinkId);
}
