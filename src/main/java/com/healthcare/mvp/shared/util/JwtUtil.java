package com.healthcare.mvp.shared.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.mvp.shared.exception.AuthenticationException;
import io.jsonwebtoken.Claims;
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
    private final ObjectMapper objectMapper;

    public JwtUtil(@Value("${app.security.jwt.secret:ThisIsAVeryLongSecretKeyForHS512AlgorithmThatIsAtLeast64BytesLong1234567890123456789}") String secret,
                   @Value("${app.security.jwt.expiration:86400}") int expiration,
                   @Value("${app.security.jwt.refresh-expiration:604800}") int refreshExpiration) {

        // Ensure the secret is at least 64 bytes for HS512
        if (secret.length() < 64) {
            log.warn("JWT secret is too short for HS512 ({}), padding to 64 characters", secret.length());
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
        this.jwtExpirationMs = expiration * 1000; // Convert to milliseconds
        this.refreshExpirationMs = refreshExpiration * 1000; // Convert to milliseconds
        this.objectMapper = new ObjectMapper();

        log.info("JWT initialized with key size: {} bits, expiration: {}ms", keyBytes.length * 8, this.jwtExpirationMs);
    }

    public String generateToken(String userId, String email, String hospitalId,
                              Collection<? extends GrantedAuthority> authorities) {
        Date expiryDate = Date.from(Instant.now().plusMillis(jwtExpirationMs));

        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
                .collect(Collectors.toList());

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("hospitalId", hospitalId);
        claims.put("roles", roles);
        claims.put("tokenType", "ACCESS_TOKEN");

        try {
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userId)
                    .setIssuedAt(new Date())
                    .setExpiration(expiryDate)
                    .setIssuer("healthcare-mvp")
                    .setAudience("healthcare-app")
                    .signWith(jwtSecret)
                    .compact();

            log.debug("Generated JWT token for user: {} with roles: {}", email, roles);
            return token;

        } catch (Exception e) {
            log.error("Failed to generate JWT token for user: {}", userId, e);
            throw new AuthenticationException("Failed to generate authentication token");
        }
    }

    public String generateRefreshToken(String userId) {
        Date expiryDate = Date.from(Instant.now().plusMillis(refreshExpirationMs));

        try {
            return Jwts.builder()
                    .setSubject(userId)
                    .claim("tokenType", "REFRESH_TOKEN")
                    .setIssuedAt(new Date())
                    .setExpiration(expiryDate)
                    .setIssuer("healthcare-mvp")
                    .setAudience("healthcare-app")
                    .signWith(jwtSecret)
                    .compact();
        } catch (Exception e) {
            log.error("Failed to generate refresh token for user: {}", userId, e);
            throw new AuthenticationException("Failed to generate refresh token");
        }
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(jwtSecret)
                    .requireIssuer("healthcare-mvp")
                    .requireAudience("healthcare-app")
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new AuthenticationException("Invalid JWT token: " + e.getMessage());
        }
    }

    public String getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("email", String.class);
    }

    public String getHospitalIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("hospitalId", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object rolesObj = claims.get("roles");

        if (rolesObj instanceof List) {
            return (List<String>) rolesObj;
        } else if (rolesObj instanceof String) {
            return Arrays.asList(((String) rolesObj).split(","));
        }

        return new ArrayList<>();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(jwtSecret)
                    .requireIssuer("healthcare-mvp")
                    .requireAudience("healthcare-app")
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Additional validation
            if (claims.getExpiration().before(new Date())) {
                log.debug("Token is expired");
                return false;
            }

            String tokenType = claims.get("tokenType", String.class);
            if (!"ACCESS_TOKEN".equals(tokenType) && !"REFRESH_TOKEN".equals(tokenType)) {
                log.debug("Invalid token type: {}", tokenType);
                return false;
            }

            log.debug("Token validation successful for user: {}", claims.getSubject());
            return true;

        } catch (Exception ex) {
            log.debug("Token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.debug("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    public long getExpirationTimeInSeconds() {
        return jwtExpirationMs / 1000;
    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}