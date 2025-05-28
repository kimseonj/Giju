package com.bubble.giju.domain.ranking.enums;

import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;

public enum Region {
    경기도,    //0
    강원도,    //1
    충청북도,  //2
    충청남도,
    전라북도,
    전라남도,
    경상북도,
    경상남도,
    제주도;

    public static Region fromCode(int code) {
        Region[] values = Region.values();
        if (code < 0 || code >= values.length) {
            throw new CustomException(ErrorCode.INVALID_REGION_CODE);
        }
        return values[code];
    }
}
