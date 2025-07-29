package com.bubble.giju.global.runner;

import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.domain.user.enums.Role;
import com.bubble.giju.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Component
public class UserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
//        if (userRepository.count() == 0) {
        if (true) {
//            loadUserData();
            log.info("사용자 데이터 로딩 완료...");
        }
    }

    private void loadUserData() {
        // 유저 데이터 생성
        log.info("사용자 데이터 로딩 중...");

        // Admin 사용자
        User admin = User.builder()
                .loginId("admin")
                .password(passwordEncoder.encode("admin"))
                .name("기주")
                .email("giju@example.com")
                .phoneNumber("01011111111")
                .birthday(LocalDate.of(2000, 1, 1))
                .createdAt(LocalDateTime.now())
                .role(Role.ADMIN)
                .build();

        // 일반 사용자들
        User user = User.builder()
                .loginId("user")
                .password(passwordEncoder.encode("user"))
                .name("김선준")
                .email("kim@example.com")
                .phoneNumber("01022222222")
                .birthday(LocalDate.of(1999, 1, 1))
                .createdAt(LocalDateTime.now())
                .role(Role.USER)
                .build();

        // 모든 사용자 저장
        userRepository.saveAll(Arrays.asList(admin, user));
    }
}
