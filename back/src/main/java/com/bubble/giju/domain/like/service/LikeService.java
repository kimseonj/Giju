package com.bubble.giju.domain.like.service;

import com.bubble.giju.domain.like.dto.LikeDto;

import java.util.List;

public interface LikeService {
    List<LikeDto.LikeResponse> getLike(String userId);
    LikeDto.LikeResponse setLike(String userId, Long drinkId, Boolean likeRequest);
}
