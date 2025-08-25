package com.healthcare.mvp.shared.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.mvp.shared.exception.AuthenticationException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey jwtSecret;
    private final int jwtExpirationMs;
    private final ObjectMapper objectMapper;

    public JwtUtil(@Value("${app.security.jwt.secret:mySecretKey123456789012345678901234567890123456789012345678901234}") String secret,
                   @Value("${app.security.jwt.expiration:86400}") int expiration) {

        // IMPORTANT: Ensure the secret is at least 64 bytes for HS512
        if (secret.length() < 64) {
            log.warn("JWT secret is too short for HS512 ({}), padding to 64 characters", secret.length());
            // Pad the secret to 64 characters if it's too short
            StringBuilder sb = new StringBuilder(secret);
            while (sb.length() < 64) {
                sb.append("0");
            }
            secret = sb.toString();
            log.info("JWT secret padded to {} characters", secret.length());
        }

        // Convert string to bytes and create the key
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // Ensure we have at least 512 bits (64 bytes)
        if (keyBytes.length < 64) {
            byte[] paddedKeyBytes = new byte[64];
            System.arraycopy(keyBytes, 0, paddedKeyBytes, 0, keyBytes.length);
            keyBytes = paddedKeyBytes;
        }

        this.jwtSecret = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationMs = expiration * 1000; // Convert seconds to milliseconds
        this.objectMapper = new ObjectMapper();

        log.info("JWT initialized with key size: {} bits", keyBytes.length * 8);
    }

    // FIXED: Method signature to match AuthenticationService calls
    public String generateToken(String userId, String email, String hospitalId,
                              List<SimpleGrantedAuthority> authorities) {
        Date expiryDate = Date.from(Instant.now().plus(jwtExpirationMs, ChronoUnit.MILLIS));

        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .collect(Collectors.toList());

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("hospitalId", hospitalId);
        claims.put("roles", roles);
        claims.put("tokenType", "ACCESS_TOKEN");
        claims.put("jti", UUID.randomUUID().toString()); // Add JTI claim

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(jwtSecret, io.jsonwebtoken.Jwts.SIG.HS512)
                .compact();
    }

    // FIXED: Method signature to match EnhancedAuthenticationService calls
    public String generateRefreshToken(UUID userId, String email) {
        Date expiryDate = Date.from(Instant.now().plus(jwtExpirationMs * 7, ChronoUnit.MILLIS)); // 7 days

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("tokenType", "REFRESH_TOKEN")
                .claim("email", email)
                .claim("jti", UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(jwtSecret, io.jsonwebtoken.Jwts.SIG.HS512)
                .compact();
    }

    // Overloaded method for backward compatibility
    public String generateRefreshToken(String userId) {
        return generateRefreshToken(UUID.fromString(userId), null);
    }

    // ADDED: Missing getJtiFromToken method
    public String getJtiFromToken(String token) {
        Map<String, Object> claims = getClaimsFromToken(token);
        return (String) claims.get("jti");
    }

    // ADDED: Missing getJwtFromRequest method
    public String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public Map<String, Object> getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(jwtSecret)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new AuthenticationException("Invalid JWT token");
        }
    }

    public String getUserIdFromToken(String token) {
        Map<String, Object> claims = getClaimsFromToken(token);
        return (String) claims.get("sub");
    }

    public String getEmailFromToken(String token) {
        Map<String, Object> claims = getClaimsFromToken(token);
        return (String) claims.get("email");
    }

    public String getHospitalIdFromToken(String token) {
        Map<String, Object> claims = getClaimsFromToken(token);
        return (String) claims.get("hospitalId");
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Map<String, Object> claims = getClaimsFromToken(token);
        return (List<String>) claims.get("roles");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(jwtSecret)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Map<String, Object> claims = getClaimsFromToken(token);
            return ((Date) claims.get("exp")).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        Map<String, Object> claims = getClaimsFromToken(token);
        return (Date) claims.get("exp");
    }

    public String generateAccessToken(UserDetails userDetails, UUID userId, String role) {
    }

    public Long getJwtExpirationMs() {
    }
}