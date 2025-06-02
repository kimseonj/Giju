package com.bubble.giju.domain.user.service;

import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.domain.user.dto.KakaoResponse;
import com.bubble.giju.domain.user.dto.OAuth2Response;
import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.domain.user.enums.Role;
import com.bubble.giju.domain.user.repository.UserRepository;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("loadUser : {}", oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("registrationId : {}", registrationId);

        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        // 나이 검증
        LocalDate birthday = LocalDate.of(oAuth2Response.getBirthYear(), oAuth2Response.getBirthDay() / 100, oAuth2Response.getBirthDay() % 100);
        if (Period.between(birthday, LocalDate.now()).getYears() < 19) {
            throw new CustomException(ErrorCode.AGE_RESTRICTION);
        }

        // 리소스 서버에서 발급 받은 정보로 사용자 ID를 만듦
        String username = registrationId + " " + oAuth2User.getName();

        Optional<User> optionalUser = userRepository.findByLoginId(username);
        if (optionalUser.isEmpty()) {
            User user = User.builder()
                    .loginId(username)
                    .name(oAuth2Response.getName())
                    .email(oAuth2Response.getEmail())
                    .phoneNumber(oAuth2Response.getPhoneNumber())
                    .birthday(LocalDate.of(oAuth2Response.getBirthYear(), oAuth2Response.getBirthDay()/100, oAuth2Response.getBirthDay()%100))
                    .role(Role.valueOf("USER"))
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(user);

            return new CustomPrincipal(user);
        } else {
            return new CustomPrincipal(optionalUser.orElseThrow(() -> new CustomException(ErrorCode.OAUTH2_UNAUTHORIZED)));
        }
    }
}
