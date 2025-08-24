package com.healthcare.mvp.shared.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

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
    private final int refreshExpirationMs;

    public JwtUtil(@Value("${app.security.jwt.secret}") String secret,
                   @Value("${app.security.jwt.expiration:86400}") int expiration,
                   @Value("${app.security.jwt.refresh-expiration:604800}") int refreshExpiration) {

        // Validate secret length for HS512 (minimum 64 bytes)
        if (secret.length() < 64) {
            throw new IllegalArgumentException("JWT secret must be at least 64 characters for HS512 algorithm");
        }

        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = expiration * 1000; // Convert to milliseconds
        this.refreshExpirationMs = refreshExpiration * 1000;

        log.info("JWT initialized with HS512 algorithm");
    }

    public String generateToken(String userId, String email, String hospitalId,
                              Collection<? extends GrantedAuthority> authorities) {
        return generateToken(userId, email, hospitalId, authorities, jwtExpirationMs, "ACCESS");
    }

    public String generateRefreshToken(String userId) {
        return generateToken(userId, null, null, Collections.emptyList(), refreshExpirationMs, "REFRESH");
    }

    private String generateToken(String userId, String email, String hospitalId,
                                Collection<? extends GrantedAuthority> authorities,
                                int expiration, String tokenType) {
        Date expiryDate = Date.from(Instant.now().plus(expiration, ChronoUnit.MILLIS));

        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("tokenType", tokenType);

        if (email != null) {
            claims.put("email", email);
        }
        if (hospitalId != null) {
            claims.put("hospitalId", hospitalId);
        }
        if (!roles.isEmpty()) {
            claims.put("roles", roles);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(jwtSecret, Jwts.SIG.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(jwtSecret)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    public Map<String, Object> getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUserIdFromToken(String token) {
        return (String) getClaimsFromToken(token).get("sub");
    }

    public String getEmailFromToken(String token) {
        return (String) getClaimsFromToken(token).get("email");
    }

    public String getHospitalIdFromToken(String token) {
        return (String) getClaimsFromToken(token).get("hospitalId");
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Object roles = getClaimsFromToken(token).get("roles");
        return roles != null ? (List<String>) roles : Collections.emptyList();
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = (Date) getClaimsFromToken(token).get("exp");
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}