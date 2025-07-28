package com.bubble.giju.domain.ranking.service.impl;

import com.bubble.giju.domain.ranking.dto.response.RegionRankingResponseDto;
import com.bubble.giju.domain.ranking.dto.response.UserRegionRankingDto;
import com.bubble.giju.domain.ranking.service.RegionRankingService;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;


@Primary
@Service
public class RedisRankingServiceImpl implements RegionRankingService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisRankingServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RegionRankingResponseDto getTop10ByRegion(String region) {
        Set<ZSetOperations.TypedTuple<String>> set = redisTemplate.opsForZSet().reverseRangeWithScores("ranking:"+region, 0, -1);

        List<UserRegionRankingDto> top10 = set.stream()
                .map(tuple -> new UserRegionRankingDto(
                        tuple.getValue(),
                        tuple.getScore().longValue()))
                .toList();

        return new RegionRankingResponseDto(region, top10);
    }
}
