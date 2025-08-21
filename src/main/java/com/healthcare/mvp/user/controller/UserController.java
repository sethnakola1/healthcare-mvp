package com.healthcare.mvp.user.controller;

import com.healthcare.mvp.business.dto.BusinessUserDto;
import com.healthcare.mvp.business.service.BusinessUserService;
import com.healthcare.mvp.shared.dto.BaseResponse;
import com.healthcare.mvp.shared.security.SecurityUtils;
import com.healthcare.mvp.user.dto.UpdateUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserController {
    private final BusinessUserService businessUserService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<BusinessUserDto>> getProfile() {
        String userId = SecurityUtils.getCurrentUserId();
        BusinessUserDto profile = businessUserService.getUserById(UUID.fromString(userId));
        return ResponseEntity.ok(BaseResponse.success("Profile retrieved", profile));
    }

//    @PutMapping("/profile")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<BaseResponse<BusinessUserDto>> updateProfile(@Valid @RequestBody UpdateUserRequest request) {
//        String userId = SecurityUtils.getCurrentUserId();
//        BusinessUserDto updated = businessUserService.updateUser(UUID.fromString(userId), request);
//        return ResponseEntity.ok(BaseResponse.success("Profile updated", updated));
//    }
}
