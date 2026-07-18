package com.starlinkiraq.store.repository;

import com.starlinkiraq.store.entity.DigitalAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface DigitalAssetRepository extends JpaRepository<DigitalAsset, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DigitalAsset> findFirstByProduct_IdAndIsUsedFalse(Long productId);

    long countByProduct_IdAndIsUsedFalse(Long productId);

    List<DigitalAsset> findByAssignedOrderId(Long orderId);
}
