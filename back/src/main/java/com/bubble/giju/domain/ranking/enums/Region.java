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
    GYEONGGI("GYEONGGI", "경기도"),
    GANGWON("GANGWON", "강원도"),
    CHUNGBUK("CHUNGBUK", "충청북도"),
    CHUNGNAM("CHUNGNAM", "충청남도"),
    JEONBUK("JEONBUK", "전라북도"),
    JEONNAM("JEONNAM", "전라남도"),
    GYEONGBUK("GYEONGBUK", "경상북도"),
    GYEONGNAM("GYEONGNAM", "경상남도"),
    JEJU("JEJU", "제주도");

    private final String code;
    private final String koreanName;

    private static final Map<String, Region> CODE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(Region::getCode, Function.identity()));
    private static final Map<String, Region> NAME_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(Region::getKoreanName, Function.identity()));

    Region(String code, String koreanName) {
        this.code = code;
        this.koreanName = koreanName;
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
    public static Region fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new CustomException(ErrorCode.REGION_CODE_NULL);
        }
        Region region = NAME_MAP.get(name.trim());
        if (region == null) {
            throw new CustomException(ErrorCode.INVALID_REGION_CODE);
        }
        return region;
    }
}

