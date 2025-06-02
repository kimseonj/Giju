package com.bubble.giju.domain.region.dto;

import lombok.Data;

@Data
public class RegionResponseDto {
    private String name;
    public RegionResponseDto(String name) {
        this.name = name;
    }
}
