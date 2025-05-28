package com.bubble.giju.domain.ranking.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRegionRankingDto {
    private String name;
    private long totalQuantity;

    @Builder
    public UserRegionRankingDto(String name, long totalQuantity) {
        this.name = name;
        this.totalQuantity = (int) totalQuantity;
    }


}
