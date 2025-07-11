package com.bubble.giju.domain.ranking.controller;

import com.bubble.giju.domain.ranking.dto.response.RegionRankingResponseDto;
import com.bubble.giju.domain.ranking.dto.response.UserRegionRankingDto;
import com.bubble.giju.domain.ranking.service.RegionRankingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegionRankingController.class)
class RegionRankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegionRankingService rankingService;

    @Test
    @DisplayName("지역 코드로 랭킹 상위 10명 조회")
    @WithMockUser(roles = "USER")
    void getRegionRanking() throws Exception {
        // given
        String regionCode = "GYEONGGI";

        List<UserRegionRankingDto> mockRanking = List.of(
                UserRegionRankingDto.builder().name("testuser1").totalQuantity(20).build(),
                UserRegionRankingDto.builder().name("testuser2").totalQuantity(15).build()
        );

        RegionRankingResponseDto mockResponse = RegionRankingResponseDto.builder()
                .region("경기도")
                .ranking(mockRanking)
                .build();

        when(rankingService.getTop10ByRegion(anyString())).thenReturn(mockResponse);

        // when/then
        mockMvc.perform(get("/api/rankings")
                        .param("regionCode", regionCode)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.region").value("경기도"))
                .andExpect(jsonPath("$.ranking[0].name").value("testuser1"))
                .andExpect(jsonPath("$.ranking[0].totalQuantity").value(20))
                .andExpect(jsonPath("$.ranking[1].name").value("testuser2"))
                .andExpect(jsonPath("$.ranking[1].totalQuantity").value(15));
    }
}