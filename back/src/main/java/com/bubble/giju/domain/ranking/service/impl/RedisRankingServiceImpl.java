package com.bubble.giju.domain.ranking.service.impl;

import com.bubble.giju.domain.ranking.dto.response.RegionRankingResponseDto;
import com.bubble.giju.domain.ranking.dto.response.UserRegionRankingDto;
import com.bubble.giju.domain.ranking.enums.Region;
import com.bubble.giju.domain.ranking.repository.RegionRankingRepository;
import com.bubble.giju.domain.ranking.service.RegionRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;


//@Primary
@Service
@RequiredArgsConstructor
public class RedisRankingServiceImpl implements RegionRankingService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RegionRankingRepository regionRankingRepository;

    @Scheduled(cron = "0 */10 * * * *")
    public void setTop10ByRegion() {
        Region[] regions = Region.values();
        for (Region region : regions) {
            String key = "ranking:" + region.getKoreanName();

            redisTemplate.delete(key);

            List<UserRegionRankingDto> top10ByRegion = regionRankingRepository.findTop10ByRegion(region.getKoreanName());

            // ZADD로 하나씩 추가 (username = member, purchaseCount = score)
            for (UserRegionRankingDto user : top10ByRegion) {
                redisTemplate.opsForZSet().add(key, user.getName(), (double) user.getTotalQuantity());
            }
        }
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
