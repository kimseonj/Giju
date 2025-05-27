package com.bubble.giju.domain.ranking.repository;

import com.bubble.giju.domain.order.entity.OrderDetail;
import com.bubble.giju.domain.ranking.dto.response.UserRegionRankingDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionRankingRepository  extends JpaRepository<OrderDetail, Long> {

    @Query("""
        SELECT new com.bubble.giju.domain.ranking.dto.response.UserRegionRankingDto(
            u.name,
            SUM(od.quantity)
        )
        FROM OrderDetail od
        JOIN od.order o
        JOIN o.user u
        WHERE od.region = :region
        AND o.orderStatus = 'DELIVERED'
        GROUP BY u.name
        ORDER BY SUM(od.quantity) DESC, MAX(o.createdAt) DESC
        LIMIT 10
        """)
    List<UserRegionRankingDto> findTop10ByRegion(@Param("region") String region);
}
