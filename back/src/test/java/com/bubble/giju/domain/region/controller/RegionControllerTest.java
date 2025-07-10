package com.bubble.giju.domain.region.controller;

import com.bubble.giju.domain.region.dto.RegionResponseDto;
import com.bubble.giju.domain.region.service.RegionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegionController.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 무시
class RegionControllerTest {

    @MockitoBean
    private RegionService regionService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
    }

    @DisplayName("지역 리스트 조회 성공 테스트")
    @Test
    void getRegions() throws Exception {
        // given
        RegionResponseDto region1 = new RegionResponseDto("서울");
        RegionResponseDto region2 = new RegionResponseDto("부산");

        List<RegionResponseDto> regionList = List.of(region1, region2);
        when(regionService.getRegions()).thenReturn(regionList);

        // when & then
        mockMvc.perform(get("/api/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("지역 리스트 조회 성공"))
                .andExpect(jsonPath("$.data[0].name").value("서울"))
                .andExpect(jsonPath("$.data[1].name").value("부산"));

    }
}