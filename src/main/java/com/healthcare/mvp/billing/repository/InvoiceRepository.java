package com.healthcare.mvp.billing.repository;

import com.healthcare.mvp.billing.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Page<Invoice> findByHospitalIdAndPatientIdAndIsActiveTrue(UUID hospitalId, UUID patientId, Pageable pageable);
    boolean existsByInvoiceIdAndHospitalIdAndIsActiveTrue(UUID invoiceId, UUID hospitalId);
}