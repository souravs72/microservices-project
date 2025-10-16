package com.microservices.apigateway.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600000}") // Default 1 hour
    private Long expiration;

    private SecretKey getSigningKey() {
        // Ensure the secret is long enough (minimum 256 bits for HS256)
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 256 bits (32 bytes)");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token format: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw e;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            return getAllClaimsFromToken(token).getSubject();
        } catch (Exception e) {
            log.error("Error extracting username from token", e);
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            return getAllClaimsFromToken(token).get("role", String.class);
        } catch (Exception e) {
            log.error("Error extracting role from token", e);
            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            return getAllClaimsFromToken(token).getExpiration();
        } catch (Exception e) {
            log.error("Error extracting expiration date from token", e);
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("Error checking token expiration", e);
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            log.warn("Token validation failed: Token expired");
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Token validation failed: Unsupported JWT");
            return false;
        } catch (MalformedJwtException e) {
            log.error("Token validation failed: Malformed JWT");
            return false;
        } catch (SignatureException e) {
            log.error("Token validation failed: Invalid signature");
            return false;
        } catch (IllegalArgumentException e) {
            log.error("Token validation failed: Illegal argument");
            return false;
        } catch (Exception e) {
            log.error("Token validation failed: Unexpected error", e);
            return false;
        }
    }

    /**
     * Validates token and returns whether it's valid
     * Also provides more detailed validation results
     */
    public ValidationResult validateTokenWithDetails(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            if (isTokenExpired(token)) {
                return new ValidationResult(false, "Token has expired");
            }

            return new ValidationResult(true, "Token is valid");
        } catch (ExpiredJwtException e) {
            return new ValidationResult(false, "Token expired");
        } catch (UnsupportedJwtException e) {
            return new ValidationResult(false, "Unsupported token format");
        } catch (MalformedJwtException e) {
            return new ValidationResult(false, "Malformed token");
        } catch (SignatureException e) {
            return new ValidationResult(false, "Invalid signature");
        } catch (Exception e) {
            return new ValidationResult(false, "Token validation failed");
        }
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}