package com.healthcare.mvp.subscription.repository;

import com.healthcare.mvp.subscription.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Page<Subscription> findByHospitalIdAndEntityTypeAndIsActiveTrue(UUID hospitalId, String entityType, Pageable pageable);
    boolean existsByEntityIdAndEntityTypeAndHospitalIdAndIsActiveTrue(UUID entityId, String entityType, UUID hospitalId);
}