package com.church.karneval.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.AlgorithmParameters;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.UUID;

@Component
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    private PublicKey ecPublicKey;

    // ✅ الصح — optional مع default value
    @Value("${supabase.jwt.secret:}")
    private String jwtSecret;

    // ES256 JWKS coordinates from Supabase
    @Value("${supabase.jwks.x:qZn0NQeaWiYkX13h9-2KpbeRVGX6_73UXBjgkf4gzWA}")
    private String jwksX;

    @Value("${supabase.jwks.y:hS6SsnBLhmCRoLBbjs9TqQOPyntkrzvfrLZNrYYjXpc}")
    private String jwksY;

    @PostConstruct
    public void init() {
        try {
            // Decode the x and y coordinates from Base64URL
            byte[] xBytes = Base64.getUrlDecoder().decode(jwksX);
            byte[] yBytes = Base64.getUrlDecoder().decode(jwksY);

            // Create the EC public key
            BigInteger x = new BigInteger(1, xBytes);
            BigInteger y = new BigInteger(1, yBytes);

            AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
            params.init(new ECGenParameterSpec("secp256r1")); // P-256
            ECParameterSpec ecSpec = params.getParameterSpec(ECParameterSpec.class);

            ECPoint point = new ECPoint(x, y);
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecSpec);

            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            this.ecPublicKey = keyFactory.generatePublic(pubSpec);

            logger.info("[JWT] Successfully initialized ES256 public key for Supabase JWT validation");
        } catch (Exception e) {
            logger.error("[JWT] Failed to initialize ES256 public key: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize JWT ES256 public key", e);
        }
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(ecPublicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.debug("[JWT] Token validation failed: {}", e.getMessage());
            return null;
        }
    }

    public UUID getUserIdFromClaims(Claims claims) {
        String sub = claims.getSubject();
        return UUID.fromString(sub);
    }

    public String getEmailFromClaims(Claims claims) {
        return claims.get("email", String.class);
    }
}
