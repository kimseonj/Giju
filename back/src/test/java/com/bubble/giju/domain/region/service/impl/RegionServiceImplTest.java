package com.bubble.giju.domain.region.service.impl;

import com.bubble.giju.domain.region.dto.RegionResponseDto;
import com.bubble.giju.domain.region.service.RegionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RegionServiceImplTest {

    @InjectMocks
    private RegionServiceImpl regionService;

    @DisplayName("지역 전체 조회 성공 테스트")
    @Test
    public void getAllRegions() {
        List<RegionResponseDto> responseDtos = regionService.getRegions();
        assertNotNull(responseDtos);
        assertEquals(responseDtos.size(),9);

    }
}