package com.bubble.giju.global.oauth2;

import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.global.jwt.CookieUtil;
import com.bubble.giju.global.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final ObjectMapper objectMapper;
    @Value("${front.uri}")
    private String FRONT_URI;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();

        String username = principal.getUsername();
        String userId = principal.getUserId();

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority().replace("ROLE_", "");

        // 토큰 발행
        String accessToken = jwtUtil.createAccessToken(username, role, userId);
        String refreshToken = jwtUtil.createRefreshToken(username, role, userId);

        // 토큰 db 저장
//        refreshTokenService.addRefreshToken(username, refreshToken, jwtUtil.getRefreshExpiration());

        // 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.addHeader("access", accessToken);
//        response.addCookie(cookieUtil.createCookie("refresh", refreshToken));
//        response.addCookie(cookieUtil.createCookie("access", accessToken));


        String domian = "giju.vercel.app";
        ResponseCookie refreshCookie = cookieUtil.createRefreshCookie("refresh", refreshToken, domian);
        ResponseCookie accessCookie = cookieUtil.createRefreshCookie("access", accessToken, domian);
//
//        response.addHeader("Set-Cookie", accessCookie.toString());
//        response.addHeader("Set-Cookie", refreshCookie.toString());
//
//        response.setStatus(HttpStatus.OK.value());
//
//        log.info("access : {}", accessCookie);
//        log.info("refresh : {}", refreshCookie);
//
//        response.sendRedirect(FRONT_URI + "/oauth/success");

        // 응답에 쿠키 추가
        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        // JSON 응답 전송
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 응답 데이터 객체 생성
        var responseData = new java.util.HashMap<String, Object>();
        var data = new java.util.HashMap<String, String>();
        data.put("access", accessToken);
        data.put("refresh", refreshToken);
        responseData.put("status", "success");
        responseData.put("data", data);

        // JSON 문자열로 변환 후 전송
        objectMapper.writeValue(response.getWriter(), responseData);

//        LoginDto.LoginResponse loginResponse = LoginDto.LoginResponse.of(accessToken, refreshToken);
//        ApiResponse<LoginDto.LoginResponse> apiResponse = ApiResponse.success("로그인 성공", loginResponse);
//
//        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
