package com.bubble.giju.domain.user.controller;

import com.bubble.giju.domain.address.dto.AddressDto;
import com.bubble.giju.domain.address.service.AddressService;
import com.bubble.giju.domain.like.dto.LikeDto;
import com.bubble.giju.domain.like.service.LikeService;
import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.domain.user.dto.UserDto;
import com.bubble.giju.domain.user.service.UserService;
import com.bubble.giju.global.config.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "일반유저 API", description = "일반 사용자 관련 API")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {

    private final UserService userService;
    private final LikeService likeService;
    private final AddressService addressService;

    // read
    @Operation(summary = "회원 불러오기", description = "현재 로그인한 회원의 정보를 불러옵니다.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<UserDto.Response>> getUser(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        UserDto.Response response = userService.find(customPrincipal.getUserId());
        ApiResponse<UserDto.Response> apiResponse = ApiResponse.success("회원불러오기 성공", response);

        return ResponseEntity.ok(apiResponse);
    }

    // update
    @Operation(summary = "회원 업데이트", description = "현재 로그인한 회원의 정보를 업데이트합니다.")
    @PatchMapping("")
    public ResponseEntity<ApiResponse<UserDto.Response>> updateUser(@AuthenticationPrincipal CustomPrincipal customPrincipal, @RequestBody UserDto.Request request) {
        log.info(request.toString());

        ApiResponse<UserDto.Response> apiResponse = ApiResponse.success(
                "회원수정 성공", userService.update(customPrincipal.getUserId(), request));

        return ResponseEntity.ok(apiResponse);
    }

    // delete
    @Operation(summary = "회원 삭제, 탈퇴", description = "현재 로그인한 회원을 삭제,탈퇴합니다.")
    @DeleteMapping("")
    public ResponseEntity<ApiResponse<String>> deleteUser(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        // TODO: 회원에 관련된 모든 테이블 Cascade
        ApiResponse<String> apiResponse = ApiResponse.success(
                "회원삭제 성공", userService.delete(customPrincipal.getUserId()));

        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "찜목록 불러오기", description = "회원이 찜한 상품들을 불러옵니다.")
    @GetMapping("/me/wishlist")
    public List<LikeDto.Response> getLike(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return likeService.getLike(customPrincipal.getUserId());
    }

    @Operation(summary = "기본 배송지 불러오기", description = "회원의 기본 배송지를 불러옵니다.")
    @GetMapping("/me/addresses/default")
    public AddressDto.Response getDefaultAddress(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return addressService.getDefaultAddress(customPrincipal.getUserId());
    }
}
