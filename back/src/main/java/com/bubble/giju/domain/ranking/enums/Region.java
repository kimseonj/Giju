package com.bubble.giju.domain.ranking.enums;

import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum Region {
    경기도("GYEONGGI"),
    강원도("GANGWON"),
    충청북도("CHUNGBUK"),
    충청남도("CHUNGNAM"),
    전라북도("JEONBUK"),
    전라남도("JEONNAM"),
    경상북도("GYEONGBUK"),
    경상남도("GYEONGNAM"),
    제주도("JEJU");

    private final String code;

    private static final Map<String, Region> CODE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(Region::getCode, Function.identity()));

    Region(String code) {
        this.code = code;
    }

    public static Region fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new CustomException(ErrorCode.REGION_CODE_NULL);
        }

        Region region = CODE_MAP.get(code.toUpperCase());
        if (region == null) {
            throw new CustomException(ErrorCode.INVALID_REGION_CODE);
        }
        return region;
    }
}

