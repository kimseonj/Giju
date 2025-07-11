package com.bubble.giju.domain.ranking.service;

import com.bubble.giju.domain.ranking.dto.response.RegionRankingResponseDto;

public interface RegionRankingService {
    RegionRankingResponseDto getTop10ByRegion(String region);
}
