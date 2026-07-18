package com.starlinkiraq.store.repository;

import com.starlinkiraq.store.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    Page<Order> findByUser_Id(Long userId, Pageable pageable);

    Optional<Order> findByIdAndUser_Id(Long id, Long userId);
}
