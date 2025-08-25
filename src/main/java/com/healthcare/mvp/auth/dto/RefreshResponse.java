package com.healthcare.mvp.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private LocalDateTime refreshTime;
    
    /**
     * Create RefreshResponse from LoginResponse
     */
    public static RefreshResponse from(LoginResponse loginResponse) {
        return RefreshResponse.builder()
                .accessToken(loginResponse.getAccessToken())
                .refreshToken(loginResponse.getRefreshToken())
                .tokenType(loginResponse.getTokenType())
                .expiresIn(loginResponse.getExpiresIn())
                .userId(loginResponse.getUserId())
                .email(loginResponse.getEmail())
                .firstName(loginResponse.getFirstName())
                .lastName(loginResponse.getLastName())
                .role(loginResponse.getRole())
                .refreshTime(LocalDateTime.now())
                .build();
    }
}