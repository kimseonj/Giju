package com.bubble.giju.domain.ranking.dto.response;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class RegionRankingResponseDto {
    private String region;
    private List<UserRegionRankingDto> ranking;

    @Builder
    public RegionRankingResponseDto(String region, List<UserRegionRankingDto> ranking) {
        this.region = region;
        this.ranking = ranking;
    }
}