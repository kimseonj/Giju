package com.bubble.giju.domain.region.service;

import com.bubble.giju.domain.region.dto.RegionResponseDto;

import java.util.List;

public interface RegionService {
    List<RegionResponseDto> getRegions();
}
