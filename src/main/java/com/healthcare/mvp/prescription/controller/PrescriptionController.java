package com.healthcare.mvp.prescription.controller;

import com.healthcare.mvp.prescription.dto.CreatePrescriptionRequest;
import com.healthcare.mvp.prescription.dto.PrescriptionDto;
import com.healthcare.mvp.prescription.service.PrescriptionService;
import com.healthcare.mvp.shared.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<BaseResponse<PrescriptionDto>> createPrescription(@Valid @RequestBody CreatePrescriptionRequest request) {
        PrescriptionDto prescription = prescriptionService.createPrescription(request);
        return ResponseEntity.ok(BaseResponse.success("Prescription created", prescription));
    }

    @GetMapping("/pdf/{prescriptionId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<byte[]> generatePrescriptionPdf(@PathVariable UUID prescriptionId) {
        byte[] pdf = prescriptionService.generatePrescriptionPdf(prescriptionId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
}