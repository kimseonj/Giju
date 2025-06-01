package com.bubble.giju.domain.user.dto;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attribute;

    public KakaoResponse(Map<String, Object> map) {
        this.attribute = map;
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
    public String getemail() {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public int getBirthYear() {
        return 0;
    }

    @Override
    public int getBirthDay() {
        return 0;
    }

    @Override
    public boolean isSignedIn() {
        return false;
    }
}
