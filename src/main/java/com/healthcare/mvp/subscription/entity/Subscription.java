package com.healthcare.mvp.subscription.entity;

import com.healthcare.mvp.business.entity.BusinessUser;
import com.healthcare.mvp.hospital.entity.Hospital;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription")
@Data
public class Subscription {
    @Id
    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId; // Hospital, Pharmacy, or Patient ID

    @Column(name = "entity_type", nullable = false)
    private String entityType; // HOSPITAL, PHARMACY, PATIENT

    @Column(name = "plan_type", nullable = false)
    private String planType; // FREE, BASIC, PREMIUM

    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, EXPIRED

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

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

    @ManyToOne
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private BusinessUser createdByAdmin;

    @ManyToOne
    @JoinColumn(name = "updated_by", insertable = false, updatable = false)
    private BusinessUser updatedByAdmin;
}