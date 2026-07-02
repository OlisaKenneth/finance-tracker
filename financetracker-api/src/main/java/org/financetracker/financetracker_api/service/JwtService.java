package org.financetracker.financetracker_api.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final String SECRET_KEY = "TXlGaW5hbmNlVHJhY2tlclN1cGVyU2VjcmV0S2V5Rm9ySldUU2lnbmluZzIwMjZLZW5uZXRo";

    // Generates a JWT token for authenticated user
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSignInKey())
                .compact();
    }

    // Extracts email from a valid JWT token
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Converts Base64 secret key to SecretKey object for signing/verification
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}