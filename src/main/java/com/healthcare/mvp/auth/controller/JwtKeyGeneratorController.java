package com.healthcare.mvp.auth.controller;

import com.healthcare.mvp.shared.dto.BaseResponse;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jwt")
@CrossOrigin(origins = "*")
@Slf4j
public class JwtKeyGeneratorController {
    
    /**
     * Generate a secure JWT key for HS512
     * Remove this endpoint in production!
     */
    @GetMapping("/generate-key")
    public BaseResponse<Map<String, Object>> generateSecureKey() {
        Map<String, Object> result = new HashMap<>();
        
        // FIXED: Using modern JJWT API without deprecated SignatureAlgorithm
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.Jwts.SIG.HS512);
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        
        // Method 2: Manual generation
        byte[] keyBytes = new byte[64]; // 64 bytes = 512 bits
        new SecureRandom().nextBytes(keyBytes);
        String manualBase64Key = Base64.getEncoder().encodeToString(keyBytes);
        
        // Method 3: Simple string (minimum 64 characters)
        StringBuilder simpleKey = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 86; i++) {
            simpleKey.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        result.put("jjwtGeneratedKey", base64Key);
        result.put("jjwtKeyLength", base64Key.length());
        result.put("manualBase64Key", manualBase64Key);
        result.put("manualKeyLength", manualBase64Key.length());
        result.put("simpleStringKey", simpleKey.toString());
        result.put("simpleKeyLength", simpleKey.length());
        
        result.put("recommendation", "Use the jjwtGeneratedKey in your application.yml");
        result.put("example", "app.security.jwt.secret: " + base64Key);
        
        log.info("Generated secure JWT keys for HS512");
        
        return BaseResponse.success("JWT keys generated successfully", result);
    }
    
    /**
     * Validate if a key is suitable for HS512
     */
    @PostMapping("/validate-key")
    public BaseResponse<Map<String, Object>> validateKey(@RequestParam String key) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("providedKey", key);
        result.put("keyLength", key.length());
        result.put("keyBits", key.length() * 8);
        
        boolean isValid = key.length() >= 64;
        result.put("isValidForHS512", isValid);
        
        if (!isValid) {
            int shortage = 64 - key.length();
            result.put("charactersShort", shortage);
            result.put("recommendation", "Add " + shortage + " more characters to meet HS512 requirements");
            
            // Provide a padded version
            StringBuilder padded = new StringBuilder(key);
            while (padded.length() < 64) {
                padded.append("X");
            }
            result.put("paddedKey", padded.toString());
        } else {
            result.put("message", "Key is valid for HS512!");
        }
        
        return BaseResponse.success("Key validation complete", result);
    }
}