package com.healthcare.mvp.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
    @NotBlank(message = "Territory is required")
    private String territory;
}