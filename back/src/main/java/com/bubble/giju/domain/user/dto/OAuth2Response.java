package com.bubble.giju.domain.user.dto;

public interface OAuth2Response {
    // 제공자
    String getProvider();
    // 제공자에서 발급하는 아이디
    String getProviderId();
    // 이메일
    String getemail();
    // 이름
    String getName();
    // 생년월일
    int getBirthYear();
    int getBirthDay();
    // 가입 여부
    boolean isSignedIn();
}
