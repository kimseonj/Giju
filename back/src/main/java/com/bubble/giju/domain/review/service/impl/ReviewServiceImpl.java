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
    public void create(String userId, Long orderId, Long drinkId, ReviewDto.Request reviewRequest) {
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new CustomException(ErrorCode.USER_UNAUTHORIZED)
        );

        Drink drink = drinkRepository.findById(drinkId).orElseThrow(
                () -> new CustomException(ErrorCode.NON_EXISTENT_DRINK)
        );

        // Todo: order 상태 점검 필요해 보임
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new CustomException(ErrorCode.NON_EXISTENT_ORDER)
        );

        // orderDetails에서 drinkName으로 주문한 상품인지 검증
        order.getOrderDetails().stream()
                .filter(detail -> detail.getDrinkName().equals(drink.getName()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_DETAIL_NOT_FOUND));

        Review review = Review.builder()
                .user(user)
                .drink(drink)
                .order(order)
                .content(reviewRequest.getContent())
                .score(reviewRequest.getScore())
                .build();

        reviewRepository.save(review);
    }

    @Override
    public List<ReviewDto.Response> getReviewsByDrinkId(Long drinkId) {
        Drink drink = drinkRepository.findById(drinkId).orElseThrow(
                () -> new CustomException(ErrorCode.NON_EXISTENT_DRINK)
        );

        List<Review> allByDrinkId = reviewRepository.findAllByDrink_Id(drinkId);

        return allByDrinkId.stream().map(ReviewDto.Response::fromEntity).toList();
    }
}
