package com.bookworm.repository;

import com.bookworm.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserIdOrderByOrderDateDesc(UUID userId);
    Optional<Order> findByOrderNumber(String orderNumber);
}
