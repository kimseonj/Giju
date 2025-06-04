package com.bubble.giju.domain.user.enums;

import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Role {
    USER,
    ADMIN;

    @JsonCreator
    public static Role fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new CustomException(ErrorCode.NON_EXISTENT_ROLE);
        }

        try {
            return Role.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.NON_EXISTENT_ROLE);
        }
    }

    @JsonValue
    public String toJson() {
        return this.name();
    }
}
