package com.bubble.giju.domain.ranking.repository;

import com.bubble.giju.domain.order.entity.Order;
import com.bubble.giju.domain.order.entity.OrderDetail;
import com.bubble.giju.domain.order.entity.OrderStatus;
import com.bubble.giju.domain.ranking.dto.response.UserRegionRankingDto;
import com.bubble.giju.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RegionRankingRepositoryTest {

    @Autowired
    RegionRankingRepository regionRankingRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("11명의 데이터를 넣으면 상위 10명만 반환")
    void findTop10ByRegion_shouldReturnTop10() {
        String region = "GYEONGGI";

        // 11명의 User + Order + OrderDetail 생성
        for (int i = 1; i <= 11; i++) {
            User user = User.builder()
                    .name("user" + i)
                    .build();
            em.persist(user); // 메모리에 저장

            Order order = Order.builder()
                    .user(user)
                    .orderName("order" + i)
                    .totalAmount(10000 + i)
                    .deliveryCharge(3000)
                    .customerKey("cusKey" + i)
                    .tossOrderId("tossOrderId" + i)
                    .build();
            order.updateStatus(OrderStatus.DELIVERED);
            em.persist(order);

            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .drinkName("drink" + i)
                    .price(1000 + i)
                    .region(region)
                    .quantity(i * 10)
                    .build();
            em.persist(orderDetail);
        }

        em.flush(); // 지금까지 쌓인 모든 변경사항을 DB에 즉시 반영
        em.clear(); // 영속성 컨텍스트 비우기 (1차 캐시 제거)

        // When
        List<UserRegionRankingDto> result = regionRankingRepository.findTop10ByRegion(region);

        // Then
        assertThat(result).hasSize(10);

        // 정렬 순서 - 내림차순 검증
        long prev = Long.MAX_VALUE;
        for (UserRegionRankingDto dto : result) {
            assertThat(dto.getTotalQuantity()).isLessThanOrEqualTo(prev);
            prev = dto.getTotalQuantity();
        }

        // 가장 높은 quantity를 가진 사람은 user11
        assertThat(result.get(0).getName()).isEqualTo("user11");
        assertThat(result.get(0).getTotalQuantity()).isEqualTo(110);
    }
}
