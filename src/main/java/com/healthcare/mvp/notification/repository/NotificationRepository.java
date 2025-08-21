package com.healthcare.mvp.notification.repository;

import com.healthcare.mvp.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByHospitalIdAndRecipientIdAndRecipientTypeAndIsActiveTrue(
        UUID hospitalId, UUID recipientId, String recipientType, Pageable pageable);
    boolean existsByNotificationIdAndHospitalIdAndIsActiveTrue(UUID notificationId, UUID hospitalId);
}