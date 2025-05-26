package com.bubble.giju.domain.ranking.service;

import com.bubble.giju.domain.ranking.dto.response.RegionRankingResponseDto;
import com.bubble.giju.domain.ranking.dto.response.UserRegionRankingDto;
import com.bubble.giju.domain.ranking.repository.RegionRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionRankingService {

    private final RegionRankingRepository rankingRepository;

    public RegionRankingResponseDto getTop10ByRegion(String region) {
        List<UserRegionRankingDto> top10 = rankingRepository.findTop10ByRegion(region);
        return new RegionRankingResponseDto(region, top10);
    }
}
