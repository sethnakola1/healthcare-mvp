package com.healthcare.mvp.audit.entity;

import com.healthcare.mvp.hospital.entity.Hospital;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_report")
@Data
public class AuditReport {
    @Id
    @Column(name = "audit_report_id")
    private UUID auditReportId;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(name = "report_type", nullable = false)
    private String reportType; // e.g., DATA_ACCESS, COMPLIANCE

    @Column(name = "report_data")
    private String reportData; // JSONB for report details

    @Column(name = "generated_date", nullable = false)
    private OffsetDateTime generatedDate;

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