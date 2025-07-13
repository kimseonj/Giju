package com.bubble.giju.domain.ranking.service;

import com.bubble.giju.domain.ranking.dto.response.RegionRankingResponseDto;
import com.bubble.giju.domain.ranking.dto.response.UserRegionRankingDto;
import com.bubble.giju.domain.ranking.repository.RegionRankingRepository;
import com.bubble.giju.domain.ranking.service.impl.RegionRankingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegionRankingServiceImplTest {

    @Mock
    private RegionRankingRepository rankingRepository;

    @InjectMocks
    private RegionRankingServiceImpl rankingService;

    private List<UserRegionRankingDto> mockRanking;

    @BeforeEach
    void setUp() {
        mockRanking = List.of(
                UserRegionRankingDto.builder()
                        .name("testuser1")
                        .totalQuantity(50)
                        .build(),
                UserRegionRankingDto.builder()
                        .name("testuesr2")
                        .totalQuantity(40)
                        .build()
        );
    }

    @Test
    @DisplayName("지역별 Top10 랭킹 조회 - 정상 케이스")
    void getTop10ByRegion() {
        // given
        String region = "경기도";
        when(rankingRepository.findTop10ByRegion(region)).thenReturn(mockRanking);

        // when
        RegionRankingResponseDto result = rankingService.getTop10ByRegion(region);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRegion()).isEqualTo(region);
        assertThat(result.getRanking()).hasSize(2);
        assertThat(result.getRanking().get(0).getName()).isEqualTo("testuser1"); //1위 값
        assertThat(result.getRanking().get(0).getTotalQuantity()).isEqualTo(50);
    }
}