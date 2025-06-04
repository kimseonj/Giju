package com.bubble.giju.domain.review.controller;

import com.bubble.giju.domain.review.dto.ReviewDto;
import com.bubble.giju.domain.review.service.ReviewService;
import com.bubble.giju.domain.user.dto.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "술에 대한 리뷰 불러오기", description = "술 상세페이지에서 리뷰를 불러옵니다.")
    @GetMapping("/drinks/{drinkId}")
    public List<ReviewDto.Response> getReviewsByDrinkId(@PathVariable Long drinkId) {
        return reviewService.getReviewsByDrinkId(drinkId);
    }

    @Operation(summary = "유저에 대한 리뷰 불러오기", description = "유저에 대한 모든 리뷰를 불러옵니다.")
    @GetMapping("/")
    public List<ReviewDto.Response> getReviews(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return reviewService.getReviewsByUserId(customPrincipal.getUserId());
    }

    @Operation(summary = "술에 대한 별점 가져오기", description = "술 상세페이지에 보여질 별점을 가져옵니다.")
    @GetMapping("/drinks/{drinkId}")
    public String getReviewScoreByDrinkId(@PathVariable Long drinkId) {
        return reviewService.getReviewScoreByDrinkId(drinkId);
    }
}
