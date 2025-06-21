package com.bubble.giju.domain.delivery.repository;

import com.bubble.giju.domain.delivery.entity.DeliveryCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryCompanyRepository extends JpaRepository<DeliveryCompany, Integer> {
    boolean existsByName(String companyName);
}
