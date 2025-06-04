package com.bubble.giju.domain.review.service;

import com.bubble.giju.domain.review.dto.ReviewDto;

import java.util.List;

public interface ReviewService {
    ReviewDto.Response create(String userId, Long orderId, Long drinkId, ReviewDto.Request reviewRequest);
    List<ReviewDto.Response> getReviewsByDrinkId(Long drinkId);
    List<ReviewDto.Response> getReviewsByUserId(String userId);
    String getReviewScoreByDrinkId(Long drinkId);
}
