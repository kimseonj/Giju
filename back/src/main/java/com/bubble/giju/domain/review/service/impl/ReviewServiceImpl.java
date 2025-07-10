package com.bubble.giju.domain.review.service.impl;

import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.drink.repository.DrinkRepository;
import com.bubble.giju.domain.order.entity.Order;
import com.bubble.giju.domain.order.repository.OrderRepository;
import com.bubble.giju.domain.review.dto.ReviewDto;
import com.bubble.giju.domain.review.entity.Review;
import com.bubble.giju.domain.review.repository.ReviewRepository;
import com.bubble.giju.domain.review.service.ReviewService;
import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.domain.user.repository.UserRepository;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final DrinkRepository drinkRepository;
    private final OrderRepository orderRepository;

    @Override
    public ReviewDto.ReviewResponse create(String userId, Long orderId, String drinkName, ReviewDto.ReviewRequest reviewRequest) {
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new CustomException(ErrorCode.USER_UNAUTHORIZED)
        );

        Drink drink = drinkRepository.findByName(drinkName).orElseThrow(
                () -> new CustomException(ErrorCode.NON_EXISTENT_DRINK)
        );

        // Todo: order 상태 점검 필요해 보임
        Order order = orderRepository.findByIdAndUser_UserId(orderId, UUID.fromString(userId)).orElseThrow(
                () -> new CustomException(ErrorCode.NON_EXISTENT_ORDER)
        );

        // orderDetails에서 drinkName으로 주문한 상품인지 검증
        order.getOrderDetails().stream()
                .filter(detail -> detail.getDrinkName().equals(drinkName))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_DETAIL_NOT_FOUND));

        Review review = Review.builder()
                .user(user)
                .drink(drink)
                .order(order)
                .content(reviewRequest.getContent())
                .score(reviewRequest.getScore())
                .build();

        Review save = reviewRepository.save(review);

        return ReviewDto.ReviewResponse.fromEntity(save);
    }

    @Override
    public List<ReviewDto.ReviewResponse> getReviewsByDrinkId(Long drinkId) {
        Drink drink = drinkRepository.findById(drinkId).orElseThrow(
                () -> new CustomException(ErrorCode.NON_EXISTENT_DRINK)
        );

        List<Review> allByDrinkId = reviewRepository.findAllByDrink_Id(drinkId);

        return allByDrinkId.stream().map(ReviewDto.ReviewResponse::fromEntity).toList();
    }

    @Override
    public List<ReviewDto.ReviewResponse> getReviewsByUserId(String userId) {
        userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new CustomException(ErrorCode.USER_UNAUTHORIZED)
        );

        return reviewRepository.findAllByUser_UserId(UUID.fromString(userId)).stream()
                .map(ReviewDto.ReviewResponse::fromEntity).toList();
    }

    @Override
    public Double getReviewScoreByDrinkId(Long drinkId) {
        Drink drink = drinkRepository.findById(drinkId).orElseThrow(
                () -> new CustomException(ErrorCode.NON_EXISTENT_DRINK)
        );

        List<Review> allByDrinkId = reviewRepository.findAllByDrink_Id(drinkId);

        double averageScore = allByDrinkId.stream().mapToInt(Review::getScore).average().orElse(0.0);

        return Double.valueOf(String.format("%.1f", averageScore));
    }
}
