package com.bubble.giju.domain.review.controller;

import com.bubble.giju.domain.review.dto.ReviewDto;
import com.bubble.giju.domain.review.service.ReviewService;
import com.bubble.giju.domain.user.dto.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리뷰 API", description = "일반 사용자 리뷰 관련 API")
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "리뷰 작성입니다.")
    @PostMapping("/orders/{orderId}")
    public void createReview(@AuthenticationPrincipal CustomPrincipal customPrincipal,
                             @PathVariable Long orderId,
                             @RequestParam Long drinkId,
                             @RequestBody ReviewDto.Request reviewRequest
                             ) {

        reviewService.create(customPrincipal.getUserId(), orderId, drinkId, reviewRequest);
    }
}
