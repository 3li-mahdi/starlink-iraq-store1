package com.starlinkiraq.store.repository;

import com.starlinkiraq.store.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProduct_IdAndIsApprovedTrue(Long productId, Pageable pageable);

    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);
}
