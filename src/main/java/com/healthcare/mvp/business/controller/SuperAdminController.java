package com.healthcare.mvp.business.controller;

import com.healthcare.mvp.business.dto.BusinessUserDto;
import com.healthcare.mvp.business.dto.CreateBusinessUserRequest;
import com.healthcare.mvp.business.service.BusinessUserService;
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
@RequestMapping("/api/business/super-admin")
@Tag(name = "Super Admin", description = "Super Admin management operations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SuperAdminController {
    
    @Autowired
    private BusinessUserService businessUserService;
    
    /**
     * Initialize first Super Admin in database
     */
    @PostMapping("/initialize")
    @Operation(summary = "Initialize first Super Admin", description = "Creates initial Super Admin: Sethna Kola")
    public ResponseEntity<BaseResponse<BusinessUserDto>> initializeSuperAdmin() {
        try {
            BusinessUserDto superAdmin = businessUserService.createInitialSuperAdmin();
            return ResponseEntity.ok(BaseResponse.success("Super Admin initialized successfully", superAdmin));
        } catch (Exception e) {
            return ResponseEntity.ok(BaseResponse.success("Super Admin already exists or initialized", null));
        }
    }

//    @PostMapping("/initialize")
//    @Operation(summary = "Initialize first Super Admin", description = "Creates initial Super Admin: Sethna Kola")
//    public ResponseEntity<BaseResponse<BusinessUserDto>> initializeSuperAdmin() {
//        BusinessUserDto superAdmin = businessUserService.createInitialSuperAdmin();
//        return ResponseEntity.ok(BaseResponse.success("Super Admin initialized successfully", superAdmin));
//    }
    
    /**
     * Create Tech Advisor
     */
    @PostMapping("/tech-advisors")
    @Operation(summary = "Create Tech Advisor", description = "Create a new Tech Advisor by Super Admin")
    public ResponseEntity<BaseResponse<BusinessUserDto>> createTechAdvisor(@Valid @RequestBody CreateBusinessUserRequest request) {
        BusinessUserDto techAdvisor = businessUserService.createTechAdvisor(request);
        return ResponseEntity.ok(BaseResponse.success("Tech Advisor created successfully", techAdvisor));
    }
    
    /**
     * Get all Tech Advisors
     */
    @GetMapping("/tech-advisors")
    @Operation(summary = "Get all Tech Advisors", description = "Retrieve list of all Tech Advisors")
    public ResponseEntity<BaseResponse<List<BusinessUserDto>>> getAllTechAdvisors() {
        List<BusinessUserDto> techAdvisors = businessUserService.getAllTechAdvisors();
        return ResponseEntity.ok(BaseResponse.success("Tech Advisors retrieved successfully", techAdvisors));
    }
    
    /**
     * Get Tech Advisor by ID
     */
    @GetMapping("/tech-advisors/{techAdvisorId}")
    @Operation(summary = "Get Tech Advisor by ID", description = "Retrieve specific Tech Advisor details")
    public ResponseEntity<BaseResponse<BusinessUserDto>> getTechAdvisorById(@PathVariable UUID techAdvisorId) {
        return businessUserService.getBusinessUserById(techAdvisorId)
                .map(techAdvisor -> ResponseEntity.ok(BaseResponse.success("Tech Advisor found", techAdvisor)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update Tech Advisor
     */
    @PutMapping("/tech-advisors/{techAdvisorId}")
    @Operation(summary = "Update Tech Advisor", description = "Update Tech Advisor details")
    public ResponseEntity<BaseResponse<BusinessUserDto>> updateTechAdvisor(
            @PathVariable UUID techAdvisorId,
            @Valid @RequestBody CreateBusinessUserRequest request) {
        BusinessUserDto updated = businessUserService.updateTechAdvisor(techAdvisorId, request);
        return ResponseEntity.ok(BaseResponse.success("Tech Advisor updated successfully", updated));
    }
    
    /**
     * Deactivate Tech Advisor
     */
    @DeleteMapping("/tech-advisors/{techAdvisorId}")
    @Operation(summary = "Deactivate Tech Advisor", description = "Deactivate a Tech Advisor")
    public ResponseEntity<BaseResponse<String>> deactivateTechAdvisor(@PathVariable UUID techAdvisorId) {
        businessUserService.deactivateTechAdvisor(techAdvisorId);
        return ResponseEntity.ok(BaseResponse.success("Tech Advisor deactivated successfully", "Deactivated"));
    }



}