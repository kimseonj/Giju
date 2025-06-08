package com.bubble.giju.domain.review.service;

import com.bubble.giju.domain.review.dto.ReviewDto;

import java.util.List;

public interface ReviewService {
    ReviewDto.ReviewResponse create(String userId, Long orderId, String drinkName, ReviewDto.ReviewRequest reviewRequest);
    List<ReviewDto.ReviewResponse> getReviewsByDrinkId(Long drinkId);
    List<ReviewDto.ReviewResponse> getReviewsByUserId(String userId);
    Double getReviewScoreByDrinkId(Long drinkId);
}
