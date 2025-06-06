package com.bubble.giju.domain.user.service;

import com.bubble.giju.domain.user.dto.UserCreateRequest;
import com.bubble.giju.domain.user.dto.UserDto;

public interface UserService {
    UserDto.Response save(UserCreateRequest userCreateRequest);
    UserDto.Response find(String userId);
    UserDto.Response update(String userId, UserDto.UserRequest userRequest);
    String delete(String userId);
}
