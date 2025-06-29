package com.bubble.giju.domain.region.controller;

import com.bubble.giju.domain.region.dto.RegionResponseDto;
import com.bubble.giju.domain.region.service.RegionService;
import com.bubble.giju.global.config.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "지역 API")
public class RegionController {

    private final RegionService regionService;

    @Operation(summary = "지역 리스트 조회",description = "지역 리스트 조회 API")
    @GetMapping("/api/regions")
    public ResponseEntity<ApiResponse<List<RegionResponseDto>>> getRegions() {
        List<RegionResponseDto> regions = regionService.getRegions();

        ApiResponse<List<RegionResponseDto>> apiResponse= ApiResponse.success("지역 리스트 조회 성공",regions);
        return ResponseEntity.ok(apiResponse);
    }
}
