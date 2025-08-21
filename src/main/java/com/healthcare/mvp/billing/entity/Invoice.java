package com.healthcare.mvp.billing.entity;

import com.healthcare.mvp.patient.entity.Patient;
import com.healthcare.mvp.user.entity.Users;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoice")
@Data
public class Invoice {
    @Id
    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

//    @Column(name = "total_amount", nullable = false)
//    private Double totalAmount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // Use BigDecimal for precise decimal handling

    @Column(name = "status", nullable = false)
    private String status; // e.g., PENDING, PAID

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
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private Users createdByAdmin;

    @ManyToOne
    @JoinColumn(name = "updated_by", insertable = false, updatable = false)
    private Users updatedByAdmin;
}
