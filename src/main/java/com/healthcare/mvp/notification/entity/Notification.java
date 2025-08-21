package com.healthcare.mvp.notification.entity;

import com.healthcare.mvp.hospital.entity.Hospital;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification")
@Data
public class Notification {
    @Id
    @Column(name = "notification_id")
    private UUID notificationId;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId; // Patient or Doctor ID

    @Column(name = "recipient_type", nullable = false)
    private String recipientType; // PATIENT, DOCTOR

    @Column(name = "notification_type", nullable = false)
    private String notificationType; // EMAIL, SMS, PUSH

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, SENT, FAILED

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false)
    private OffsetDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private OffsetDateTime updatedDate;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "hospital_id", insertable = false, updatable = false)
    private Hospital hospital;

//    @ManyToOne
//    @JoinColumn(name = "created_by", insertable = false, updatable = false)
//    private Users createdByAdmin;
//
//    @ManyToOne
//    @JoinColumn(name = "updated_by", insertable = false, updatable = false)
//    private Users updatedByAdmin;
}