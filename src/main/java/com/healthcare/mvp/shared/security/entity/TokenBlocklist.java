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
@Table(name = "token_blocklist", indexes = {
    @Index(name = "idx_token_id", columnList = "tokenId"),
    @Index(name = "idx_expires_at", columnList = "expiresAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlocklist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String tokenId; // JWT ID claim
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @CreationTimestamp
    private LocalDateTime blockedAt;
    
    private String reason; // logout, security_breach, etc.
}