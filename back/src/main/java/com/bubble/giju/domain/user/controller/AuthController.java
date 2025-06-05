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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 인증 API", description = "회원가입 & 토큰 재발급")
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
}
