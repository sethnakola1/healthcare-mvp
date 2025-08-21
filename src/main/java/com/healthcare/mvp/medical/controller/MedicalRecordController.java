package com.healthcare.mvp.medical.controller;

import com.healthcare.mvp.medical.dto.CreateMedicalRecordRequest;
import com.healthcare.mvp.medical.dto.MedicalRecordDto;
import com.healthcare.mvp.medical.service.MedicalRecordService;
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
@RequestMapping("/api/medical-records")
@Tag(name = "Medical Records", description = "Patient medical records management")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class MedicalRecordController {
    
    private final MedicalRecordService medicalRecordService;
    
    /**
     * Create medical record - Doctor only
     */
    @PostMapping
    @Operation(summary = "Create Medical Record", description = "Create a new medical record after consultation")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<BaseResponse<MedicalRecordDto>> createMedicalRecord(
            @Valid @RequestBody CreateMedicalRecordRequest request) {
        MedicalRecordDto record = medicalRecordService.createMedicalRecord(request);
        return ResponseEntity.ok(BaseResponse.success("Medical record created successfully", record));
    }
    
    /**
     * Get patient's medical history - Doctor, Nurse, or Hospital Admin
     */
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get Patient Medical History", description = "Get complete medical history for a patient")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE') or hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<BaseResponse<List<MedicalRecordDto>>> getPatientMedicalHistory(
            @PathVariable UUID patientId) {
        List<MedicalRecordDto> records = medicalRecordService.getPatientMedicalHistory(patientId);
        return ResponseEntity.ok(BaseResponse.success("Medical history retrieved successfully", records));
    }
    
    /**
     * Get medical record by ID - Doctor, Nurse, or Hospital Admin
     */
    @GetMapping("/{recordId}")
    @Operation(summary = "Get Medical Record", description = "Get specific medical record details")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE') or hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<BaseResponse<MedicalRecordDto>> getMedicalRecord(@PathVariable UUID recordId) {
        return medicalRecordService.getMedicalRecordById(recordId)
                .map(record -> ResponseEntity.ok(BaseResponse.success("Medical record found", record)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update medical record - Only the doctor who created it
     */
    @PutMapping("/{recordId}")
    @Operation(summary = "Update Medical Record", description = "Update medical record (only by creator)")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<BaseResponse<MedicalRecordDto>> updateMedicalRecord(
            @PathVariable UUID recordId,
            @Valid @RequestBody CreateMedicalRecordRequest request) {
        MedicalRecordDto updated = medicalRecordService.updateMedicalRecord(recordId, request);
        return ResponseEntity.ok(BaseResponse.success("Medical record updated successfully", updated));
    }
}