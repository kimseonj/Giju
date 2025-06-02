package com.bubble.giju.domain.user.dto;

import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> map;
    private final Map<String, Object> attribute;

    public KakaoResponse(Map<String, Object> map) {
        this.map = map;
        this.attribute = (Map<String, Object>) map.get("kakao_account");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return "";
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getName() {
        return Optional.of(attribute.get("profile"))
                .map(Map.class::cast)
                .map(profile -> profile.get("nickname"))
                .map(String::valueOf).orElse("");
    }

    @Override
    public String getPhoneNumber() {
        return "";
//        return attribute.get("phone_number").toString();
    }

    @Override
    public int getBirthYear() {
        return Integer.parseInt(attribute.get("birthyear").toString());
    }

    @Override
    public int getBirthDay() {
        return Integer.parseInt(attribute.get("birthday").toString());
    }

    public String getGender() {
        return attribute.get("gender").toString().toUpperCase();
    }

    public LocalDateTime connectedAt() { return map.get("connected_at") == null ? LocalDateTime.now() : (LocalDateTime) map.get("connected_at"); }
}
