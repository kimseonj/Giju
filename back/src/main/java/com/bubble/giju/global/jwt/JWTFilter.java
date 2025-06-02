package com.bubble.giju.global.jwt;

import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.domain.user.enums.Role;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 현재 요청 경로 추출
        String requestURI = request.getRequestURI();

        // request에서 Authorization 헤더 추출
        String accssToken = request.getHeader("access");

        // Authorization 검증
        if (accssToken == null) {
//            if (requestURI.startsWith("/api/auth")) {
                filterChain.doFilter(request, response);

                return;
//            }
//
//            throw new CustomException(ErrorCode.INVALID_access);
        }

        // 토큰 검증 시작
        try {
            jwtUtil.isExpired(accssToken);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_JWT);
        }

        // 토큰에서 username과 role을 획득
        String username = jwtUtil.getUsername(accssToken);
        Role role = Role.valueOf(jwtUtil.getRole(accssToken));
        String userId = jwtUtil.getUserId(accssToken);

        // userEntity 생성
        User user = User.builder()
                .userId(UUID.fromString(userId))
                .loginId(username)
                .role(role)
                .build();

        // UserDetails에 회원정보 객체 담기
        CustomPrincipal customPrincipal = new CustomPrincipal(user);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customPrincipal, null, customPrincipal.getAuthorities());
        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}

