package com.bubble.giju.domain.like.controller;

import com.bubble.giju.domain.like.dto.LikeDto;
import com.bubble.giju.domain.like.service.LikeService;
import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.global.config.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "찜 API", description = "주류 찜 API 입니다.")
@RequestMapping("/api/drinks")
@RequiredArgsConstructor
@RestController
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "찜하기", description = "회원이 빈하트를 누르면 찜하기 됩니다.")
    @PostMapping("/{drinkId}/wishlist")
    public ResponseEntity<ApiResponse<?>> addLike(@AuthenticationPrincipal CustomPrincipal customPrincipal, @PathVariable Long drinkId) {
        LikeDto.Response response = likeService.setLike(customPrincipal.getUserId(), drinkId, true);

        ApiResponse<LikeDto.Response> apiResponse = ApiResponse.success("찜하기 성공 완료", response);

        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "찜하기", description = "회원이 채워진하트를 누르면 찜하기 취소 됩니다.")
    @DeleteMapping("/{drinkId}/wishlist")
    public ResponseEntity<ApiResponse<?>> deleteLike(@AuthenticationPrincipal CustomPrincipal customPrincipal, @PathVariable Long drinkId) {
        LikeDto.Response response = likeService.setLike(customPrincipal.getUserId(), drinkId, false);

        ApiResponse<LikeDto.Response> apiResponse = ApiResponse.success("찜하기 취소 완료", response);

        return ResponseEntity.ok(apiResponse);
    }
}
