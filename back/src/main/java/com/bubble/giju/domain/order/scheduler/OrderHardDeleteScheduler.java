package com.bubble.giju.domain.order.scheduler;

import com.bubble.giju.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderHardDeleteScheduler {

    private final OrderRepository orderRepository;

    private static final int CHUNK_SIZE = 100;

    //특정블럭만 트랜잭션을 관리
    private final PlatformTransactionManager transactionManager;
    @Value("${order.delete-cycle}")
    private int orderDeltedCycle;

    /**
     * 매일 새벽 3시에 실행됨
     * 30일이 지난 soft-deleted 주문 관련 데이터 완전 삭제
     */

    @Scheduled(cron = "0 0 3 * * ?")
    public void hardDeleteExpiredSoftDeletedOrders() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(orderDeltedCycle);

        // 30일 이상 soft delete된 주문 id 조회 ->  불필요한 쿼리 실행을 막기 위함
        List<Long> orderIdsToDelete = orderRepository.findIdsBySoftDeletedBefore(cutoff);
        if (orderIdsToDelete.isEmpty()) {
            log.info("[HardDelete] 삭제 대상 주문 없음");
            return;
        }
        log.info("[HardDelete] 총 삭제 대상 주문 수: {}", orderIdsToDelete.size());

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

        for (int i = 0; i < orderIdsToDelete.size(); i += CHUNK_SIZE) {
            List<Long> chunk = orderIdsToDelete.subList(
                    i,
                    Math.min(i + CHUNK_SIZE, orderIdsToDelete.size())
            );

            /** 트랜잭션 템플릿을 사용하여 트랜잭션 안에서 안전하게 실행
             *  이 블럭 안의 코드는 하나의 트랜잭션으로 실행됨
             *  트랜잭션 블럭을 벗어나면 자동으로 commit됨
             *  만약 예외가 발생하면 자동으로 rollback됨
             */
            txTemplate.executeWithoutResult(status -> orderRepository.hardDeleteByOrderIds(chunk));

            log.info("[HardDelete] {} ~ {}번 주문 삭제 처리 완료", i + 1, i + chunk.size());
        }

        log.info("[HardDelete] 모든 soft-deleted 주문 완전 삭제 완료");
    }
}
