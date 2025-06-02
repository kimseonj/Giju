package com.bubble.giju.domain.like.service;

import com.bubble.giju.domain.like.dto.LikeDto;

import java.util.List;

public interface LikeService {
    List<LikeDto.Response> getLike(String userId);
    LikeDto.Response toggleLike(String userId, Long drinkId, Boolean likeRequest);
}
