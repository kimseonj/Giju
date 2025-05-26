package com.bubble.giju.global.config;

import com.bubble.giju.global.jwt.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final CookieUtil cookieUtil;

    // CORS Configuration을 Bean으로 등록
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 허용할 오리진(출처) 설정
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));

        // 허용할 HTTP 메서드 설정
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));

        // 허용할 HTTP 헤더 설정
        configuration.setAllowedHeaders(Collections.singletonList("*"));
//        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With",
//                "Accept", "Origin", "Access-Control-Request-Method",
//                "Access-Control-Request-Headers"));
        // 자격 증명(쿠키, HTTP 인증) 허용 설정
        configuration.setAllowCredentials(true);
        // 브라우저에 노출할 헤더 설정
        configuration.setExposedHeaders(Arrays.asList("access", "refresh", "Content-Type"));
        // 프리플라이트 요청 캐시 시간(초)
        configuration.setMaxAge(3600L);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        // CORS 설정 적용 - Bean으로 등록한 corsConfigurationSource 사용
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http
                .csrf(auth -> {
                    auth.disable();
                    System.out.println("Csrf enabled");
                })
                .formLogin(auth -> auth.disable())
                .httpBasic(auth -> auth.disable())
                // session 설정 -> stateless로 변경
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // h2 console
        http
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        // 경로별 인가 작업
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/error","/api/categories","/api/rankings").permitAll()
                        .requestMatchers("/swagger-ui/**", "/api/swagger-config/**", "/v3/api-docs/**",
                                "/h2-console/**",
                                "/favicon.ico",
                                "/error",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**").permitAll()
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        .anyRequest().authenticated()
                );

        // login filter 등록
        http
                .addFilterAt(new LoginFilter(authenticationConfiguration.getAuthenticationManager(), jwtUtil, cookieUtil, objectMapper), UsernamePasswordAuthenticationFilter.class);

        // JWT Filter 등록
        http
                .addFilterAfter(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JWTExceptionHandler(), LoginFilter.class); // JWTFilter 앞에 예외 처리 필터 추가

        return http.build();
    }

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Giju API")
                .version("v1.0")
                .description("Giju API 문서");

        // JWT 인증 설정
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .bearerFormat("JWT")
                .name("access")
                .description("access 토큰을 입력하세요");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("accessAuth");

        return new OpenAPI()
                .info(info)
                .components(new Components().addSecuritySchemes("accessAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }

}