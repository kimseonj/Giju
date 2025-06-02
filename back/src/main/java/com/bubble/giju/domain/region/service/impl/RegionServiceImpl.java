package com.bubble.giju.domain.region.service.impl;

import com.bubble.giju.domain.ranking.enums.Region;
import com.bubble.giju.domain.region.dto.RegionResponseDto;
import com.bubble.giju.domain.region.service.RegionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RegionServiceImpl implements RegionService {
    @Override
    public List<RegionResponseDto> getRegions() {
        List<RegionResponseDto> regions = new ArrayList<>();

        for (Region region : Region.values()) {
            regions.add(new RegionResponseDto(region.getKoreanName()));
        }
        return regions;
    }
}
