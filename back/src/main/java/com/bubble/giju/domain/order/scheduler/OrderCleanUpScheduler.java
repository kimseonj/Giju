package com.bubble.giju.domain.order.scheduler;

import com.bubble.giju.domain.order.entity.Order;
import com.bubble.giju.domain.order.entity.OrderStatus;
import com.bubble.giju.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCleanUpScheduler {

    private final OrderRepository orderRepository;
    private static final int PAGE_SIZE = 100;

    @Value("${order.cleanup-expiration-minutes}")
    private int expirationMinutes;

    @Scheduled(fixedRate = 5 * 60 * 1000) // 5분 간격
    @Transactional
    public void CleanUpExpiredPendingOrder(){
        OffsetDateTime cutoff =  OffsetDateTime.now().minusMinutes(expirationMinutes);
        log.info("[OrderCleanUpScheduler] {}분 경과 주문 소프트 삭제 시작 - 기준 시각: {}", expirationMinutes, cutoff);

        int pageNumber = 0;
        Page<Order> expiredOrdersPage;

        do {
            PageRequest pageRequest = PageRequest.of(pageNumber++, PAGE_SIZE);
            expiredOrdersPage = orderRepository.findByOrderStatusAndCreatedAtBeforeAndIsDeletedFalse(
                    OrderStatus.PENDING, cutoff, pageRequest
            );

            if (expiredOrdersPage.isEmpty()) {
                log.info("페이지 {}: 삭제 대상 없음", pageNumber);
            }
            expiredOrdersPage.forEach(this::softDeleteOrderFlow);

        } while (!expiredOrdersPage.isEmpty());
        log.info("[OrderCleanUpScheduler] 소프트 삭제 완료");
    }

    @Transactional
    public void softDeleteOrderFlow(Order order) {
        log.info("softDelete 대상 주문 ID: {}, 생성일: {}", order.getId(), order.getCreatedAt());
        order.softDelete();
        orderRepository.save(order);

        log.info("주문 ID {} soft delete 완료", order.getId());
    }
}
