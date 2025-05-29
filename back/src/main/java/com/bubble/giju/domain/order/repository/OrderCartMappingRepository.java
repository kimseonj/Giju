package com.bubble.giju.domain.order.repository;

import com.bubble.giju.domain.order.entity.Order;
import com.bubble.giju.domain.order.entity.OrderCartMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderCartMappingRepository extends JpaRepository<OrderCartMapping, Long> {

    List<OrderCartMapping> findByOrder(Order order);

    void deleteByOrder(Order order);
}