package com.healthcare.mvp.audit.repository;

import com.healthcare.mvp.audit.entity.AuditReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditReportRepository extends JpaRepository<AuditReport, UUID> {
    Page<AuditReport> findByHospitalIdAndReportTypeAndIsActiveTrue(UUID hospitalId, String reportType, Pageable pageable);
    boolean existsByAuditReportIdAndHospitalIdAndIsActiveTrue(UUID auditReportId, UUID hospitalId);
}