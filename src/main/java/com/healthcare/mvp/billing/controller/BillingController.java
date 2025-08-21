package com.healthcare.mvp.billing.controller;

import com.healthcare.mvp.billing.dto.BillingDto;
import com.healthcare.mvp.billing.dto.CreateBillRequest;
import com.healthcare.mvp.billing.service.BillingService;
import com.healthcare.mvp.shared.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
@Tag(name = "Billing Management", description = "Patient billing and invoicing")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class BillingController {
    
    private final BillingService billingService;
    
    /**
     * Create bill - Hospital Admin, Billing Staff, or Receptionist
     */
    @PostMapping
    @Operation(summary = "Create Bill", description = "Create a new bill for patient services")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('BILLING_STAFF') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BaseResponse<BillingDto>> createBill(
            @Valid @RequestBody CreateBillRequest request) {
        BillingDto bill = billingService.createBill(request);
        return ResponseEntity.ok(BaseResponse.success("Bill created successfully", bill));
    }
    
    /**
     * Get hospital bills - Hospital Admin or Billing Staff
     */
    @GetMapping("/hospital/{hospitalId}")
    @Operation(summary = "Get Hospital Bills", description = "Get all bills for a hospital")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('BILLING_STAFF')")
    public ResponseEntity<BaseResponse<List<BillingDto>>> getHospitalBills(
            @PathVariable UUID hospitalId) {
        List<BillingDto> bills = billingService.getHospitalBills(hospitalId);
        return ResponseEntity.ok(BaseResponse.success("Bills retrieved successfully", bills));
    }
    
    /**
     * Get patient bills - Patient, Hospital Admin, or Billing Staff
     */
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get Patient Bills", description = "Get all bills for a patient")
    @PreAuthorize("hasRole('PATIENT') or hasRole('HOSPITAL_ADMIN') or hasRole('BILLING_STAFF') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BaseResponse<List<BillingDto>>> getPatientBills(
            @PathVariable UUID patientId) {
        List<BillingDto> bills = billingService.getPatientBills(patientId);
        return ResponseEntity.ok(BaseResponse.success("Patient bills retrieved successfully", bills));
    }
    
    /**
     * Update bill payment status - Hospital Admin or Billing Staff
     */
    @PutMapping("/{billId}/payment-status")
    @Operation(summary = "Update Payment Status", description = "Update bill payment status")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('BILLING_STAFF') or hasRole('RECEPTIONIST')")
    public ResponseEntity<BaseResponse<BillingDto>> updatePaymentStatus(
            @PathVariable UUID billId,
            @RequestParam String paymentStatus) {
        BillingDto updated = billingService.updatePaymentStatus(billId, paymentStatus);
        return ResponseEntity.ok(BaseResponse.success("Payment status updated successfully", updated));
    }
}