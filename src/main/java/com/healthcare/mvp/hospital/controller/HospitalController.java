package com.healthcare.mvp.hospital.controller;

import com.healthcare.mvp.hospital.dto.CreateHospitalRequest;
import com.healthcare.mvp.hospital.dto.HospitalDto;
import com.healthcare.mvp.hospital.service.HospitalService;
import com.healthcare.mvp.shared.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals")
@Tag(name = "Hospital Management", description = "Hospital CRUD operations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HospitalController {
    
    @Autowired
    private HospitalService hospitalService;
    
    /**
     * Create Hospital (by Super Admin or Tech Advisor)
     */
    @PostMapping
    @Operation(summary = "Create Hospital", description = "Create a new hospital by Super Admin or Tech Advisor")
    public ResponseEntity<BaseResponse<HospitalDto>> createHospital(
            @Valid @RequestBody CreateHospitalRequest request,
            @RequestParam UUID createdByBusinessUserId) {
        HospitalDto hospital = hospitalService.createHospital(request, createdByBusinessUserId);
        return ResponseEntity.ok(BaseResponse.success("Hospital created successfully", hospital));
    }
    
    /**
     * Get all hospitals
     */
    @GetMapping
    @Operation(summary = "Get all hospitals", description = "Retrieve list of all active hospitals")
    public ResponseEntity<BaseResponse<List<HospitalDto>>> getAllHospitals() {
        List<HospitalDto> hospitals = hospitalService.getAllHospitals();
        return ResponseEntity.ok(BaseResponse.success("Hospitals retrieved successfully", hospitals));
    }
    
    /**
     * Get hospitals by business user
     */
    @GetMapping("/business-user/{businessUserId}")
    @Operation(summary = "Get hospitals by business user", description = "Get hospitals brought by specific Tech Advisor")
    public ResponseEntity<BaseResponse<List<HospitalDto>>> getHospitalsByBusinessUser(@PathVariable String partnerCodeUsed) {
        List<HospitalDto> hospitals = hospitalService.getHospitalsByBusinessUser(partnerCodeUsed);
        return ResponseEntity.ok(BaseResponse.success("Hospitals retrieved successfully", hospitals));
    }
    
    /**
     * Get hospital by ID
     */
    @GetMapping("/{hospitalId}")
    @Operation(summary = "Get hospital by ID", description = "Retrieve specific hospital details")
    public ResponseEntity<BaseResponse<HospitalDto>> getHospitalById(@PathVariable UUID hospitalId) {
        return hospitalService.getHospitalById(hospitalId)
                .map(hospital -> ResponseEntity.ok(BaseResponse.success("Hospital found", hospital)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get hospital by code
     */
    @GetMapping("/code/{hospitalCode}")
    @Operation(summary = "Get hospital by code", description = "Retrieve hospital by hospital code (HOS00001)")
    public ResponseEntity<BaseResponse<HospitalDto>> getHospitalByCode(@PathVariable String hospitalCode) {
        return hospitalService.getHospitalByCode(hospitalCode)
                .map(hospital -> ResponseEntity.ok(BaseResponse.success("Hospital found", hospital)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update hospital
     */
    @PutMapping("/{hospitalId}")
    @Operation(summary = "Update hospital", description = "Update hospital details")
    public ResponseEntity<BaseResponse<HospitalDto>> updateHospital(
            @PathVariable UUID hospitalId,
            @Valid @RequestBody CreateHospitalRequest request) {
        HospitalDto updated = hospitalService.updateHospital(hospitalId, request);
        return ResponseEntity.ok(BaseResponse.success("Hospital updated successfully", updated));
    }
    
    /**
     * Deactivate hospital
     */
    @DeleteMapping("/{hospitalId}")
    @Operation(summary = "Deactivate hospital", description = "Deactivate a hospital")
    public ResponseEntity<BaseResponse<String>> deactivateHospital(@PathVariable UUID hospitalId) {
        hospitalService.deactivateHospital(hospitalId);
        return ResponseEntity.ok(BaseResponse.success("Hospital deactivated successfully", "Deactivated"));
    }
}