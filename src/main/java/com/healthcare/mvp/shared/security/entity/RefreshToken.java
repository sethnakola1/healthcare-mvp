package com.healthcare.mvp.shared.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(unique = true, nullable = false, length = 512)
    private String token;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private String deviceFingerprint;
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Builder.Default
    private Boolean revoked = false;
    
    // Token family ID for rotation tracking
    private String familyId;
    
    // Previous token hash for security
    private String previousTokenHash;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}