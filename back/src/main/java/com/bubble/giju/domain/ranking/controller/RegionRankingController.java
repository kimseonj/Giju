package com.bubble.giju.domain.ranking.controller;

import com.bubble.giju.domain.ranking.dto.response.RegionRankingResponseDto;
import com.bubble.giju.domain.ranking.enums.Region;
import com.bubble.giju.domain.ranking.service.RegionRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
@Tag(name = "지역별 주류 구매 랭킹 API")
public class RegionRankingController {

    private final RegionRankingService rankingService;

    @GetMapping
    @Operation(summary = "지역별 랭킹 조회", description = "지역 코드(regionCode)를 기준으로 상위 10명의 사용자 구매 랭킹을 조회 ")
    public ResponseEntity<RegionRankingResponseDto> getRegionRanking(@RequestParam int regionCode) {
        Region region = Region.fromCode(regionCode);
        RegionRankingResponseDto response = rankingService.getTop10ByRegion(region.name());
        return ResponseEntity.ok(response);
    }
}
