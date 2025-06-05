package com.bubble.giju.domain.user.service.impl;

import com.bubble.giju.domain.user.dto.UserCreateRequest;
import com.bubble.giju.domain.user.dto.UserDto;
import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.domain.user.repository.UserRepository;
import com.bubble.giju.domain.user.service.UserService;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @Transactional
    public UserDto.Response save(UserCreateRequest userCreateRequest) {
        if (userRepository.findByLoginId(userCreateRequest.getLoginId()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_USER_LoginId);
        }

        User user = User.builder()
                .loginId(userCreateRequest.getLoginId())
                .password(bCryptPasswordEncoder.encode(userCreateRequest.getPassword()))
                .name(userCreateRequest.getName())
                .email(userCreateRequest.getEmail())
                .phoneNumber(userCreateRequest.getPhoneNumber())
                .birthday(userCreateRequest.getBirthDay())
                .role(userCreateRequest.getRole())
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        return UserDto.Response.fromEntity(user);
    }

    @Transactional
    @Override
    public UserDto.Response find(String userId) {
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new CustomException(ErrorCode.NON_EXISTENT_USER)
        );

        return UserDto.Response.fromEntity(user);
    }

    @Transactional
    @Override
    public UserDto.Response update(String userId, UserDto.UserRequest userRequest) {
//        if (!userId.equals(userRequest.getUserId())) {
        // Todo: 질문. 어느정도까지 자세히 에러를 분류할 것 인가? 프론트를 위한 에러?
//            throw new CustomException(ErrorCode.NON_EXISTENT_USER);
//        }

        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new CustomException(ErrorCode.NON_EXISTENT_USER)
        );

        user.update(userRequest);
        log.info("Updated user: {}", user); // 변경 확인용 로그

        return UserDto.Response.fromEntity(user);
    }

    @Transactional
    @Override
    public String delete(String userId) {
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new CustomException(ErrorCode.NON_EXISTENT_USER)
        );

        userRepository.deleteById(UUID.fromString(userId));

        return user.getLoginId();
    }

}
