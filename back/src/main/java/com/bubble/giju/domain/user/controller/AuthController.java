package com.bubble.giju.domain.user.controller;

import com.bubble.giju.domain.user.dto.LoginDto;
import com.bubble.giju.domain.user.dto.UserCreateRequest;
import com.bubble.giju.domain.user.dto.UserDto;
import com.bubble.giju.domain.user.service.UserService;
import com.bubble.giju.global.config.ApiResponse;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import com.bubble.giju.global.jwt.CookieUtil;
import com.bubble.giju.global.jwt.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 인증 API", description = "회원가입 & 토큰 재발급")
@Slf4j
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final UserService userService;
    private final JWTUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @Operation(summary = "회원가입", description = "회원가입입니다.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto.Response>> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        System.out.println(userCreateRequest);

        UserDto.Response response = userService.save(userCreateRequest);

        ApiResponse<UserDto.Response> apiResponse = ApiResponse.success("회원가입 성공", response);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Access 토큰 재발급", description = "쿠키의 Refresh로 Access 토큰을 재발급 받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginDto.LoginResponse>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            throw new CustomException(ErrorCode.INVALID_refresh);
        }

        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            throw new CustomException(ErrorCode.INVALID_refresh);
        }

        String role = jwtUtil.getRole(refresh);
        String username = jwtUtil.getUsername(refresh);
        String userId = jwtUtil.getUserId(refresh);

        String newAccessToken = jwtUtil.createAccessToken(username, role, userId);
        String newRefreshToken = jwtUtil.createRefreshToken(username, role, userId);

        LoginDto.LoginResponse loginResponse = LoginDto.LoginResponse.of(newAccessToken, newRefreshToken);
        ApiResponse<LoginDto.LoginResponse> apiResponse = ApiResponse.success("JWT 재발급 성공", loginResponse);

        // test
        response.setHeader("access", newAccessToken);
        response.addCookie(cookieUtil.createCookie("refresh", newRefreshToken));

        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "OAuth2 토큰 교환", description = "HttpOnly 쿠키에서 토큰을 읽어서 반환합니다.")
    @PostMapping("/exchange-token")
    public ResponseEntity<ApiResponse<LoginDto.LoginResponse>> exchangeToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("=== 토큰 교환 API 호출 ===");

        // HttpOnly 쿠키에서 토큰 읽기
        String accessToken = getCookieValue(request, "access");
        String refreshToken = getCookieValue(request, "refresh");

        log.info("쿠키에서 읽은 access 토큰: {}", accessToken != null ? "존재함" : "없음");
        log.info("쿠키에서 읽은 refresh 토큰: {}", refreshToken != null ? "존재함" : "없음");

        if (accessToken != null && refreshToken != null) {
//            response.addCookie(cookieUtil.deleteCookie("access"));
//            response.addCookie(cookieUtil.deleteCookie("refresh"));
            log.info("모든 인증 쿠키 삭제 완료");

            LoginDto.LoginResponse loginResponse = LoginDto.LoginResponse.of(accessToken, refreshToken);
            ApiResponse<LoginDto.LoginResponse> apiResponse = ApiResponse.success("토큰 교환 성공", loginResponse);

            log.info("토큰 교환 성공");
            return ResponseEntity.ok(apiResponse);
        }

        log.warn("토큰 교환 실패: 쿠키에서 토큰을 찾을 수 없음");
        throw new CustomException(ErrorCode.INVALID_refresh); // 또는 적절한 ErrorCode
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        log.info("쿠키 '{}' 찾는 중...", name);

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                log.info("쿠키 발견: {}={}", cookie.getName(), cookie.getValue());
                if (name.equals(cookie.getName())) {
                    log.info("원하는 쿠키 '{}' 찾음", name);
                    return cookie.getValue();
                }
            }
        }

        log.info("쿠키 '{}'를 찾을 수 없음", name);
        return null;
    }

}
