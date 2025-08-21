package com.healthcare.mvp.patient.entity;

import com.healthcare.mvp.business.entity.BusinessUser;
import com.healthcare.mvp.doctor.entity.Doctor;
import com.healthcare.mvp.user.entity.Users;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "encounter")
@Data
public class Encounter {
    @Id
    @Column(name = "encounter_id")
    private UUID encounterId;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "visit_date", nullable = false)
    private OffsetDateTime visitDate;

    @Column(name = "symptoms")
    private String symptoms; // JSONB or text for symptoms

    @Column(name = "initial_investigations")
    private String initialInvestigations; // JSONB or text for lab results

    @Column(name = "fhir_resource_id")
    private String fhirResourceId; // For FHIR integration

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
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private Users createdByAdmin;

    @ManyToOne
    @JoinColumn(name = "updated_by", insertable = false, updatable = false)
    private Users updatedByAdmin;
}