package com.bubble.giju.domain.user.dto;

public interface OAuth2Response {
    // 제공자
    String getProvider();
    // 제공자에서 발급하는 아이디
    String getProviderId();
    // 이메일
    String getEmail();
    // 이름
    String getName();
    // 전화번호
    String getPhoneNumber();
    // 생년월일
    int getBirthYear();
    int getBirthDay();
}
