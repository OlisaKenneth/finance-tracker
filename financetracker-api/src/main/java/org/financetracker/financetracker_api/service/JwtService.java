package org.financetracker.financetracker_api.service;


import io.jsonwebtoken.Jwts; //<- the main tool for building and reading tokens
import io.jsonwebtoken.io.Decoders; //<- helps convert our secret key from text into usable bytes
import io.jsonwebtoken.security.Keys; //<- helps build a secure key object from raw bytes
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey; //<- the actual secret key type used to sign/verify tokens
import java.util.Date; //<- used to set when the token was created and when it expires

/*
 * This class is our TOKEN FACTORY
 * It creates JWT tokens after a successful login
 * and can read tokens to check who they belong to
 *
 * Think of it like a wristband stamping machine at a concert:
 * - Stamps a wristband with your name and an expiry time
 * - Can also CHECK a wristband to see if it's real and not expired
 */
@Service
public class JwtService {

    // this is our SECRET KEY — used to "sign" every token
    // so we can tell if a token was really created by US
    // and not faked by someone else
    // in a real production app this would come from an environment variable,
    // not be hardcoded — we'll improve this later
    private final String SECRET_KEY = "ThisIsASecretKeyThatShouldBeMuchLongerAndStoredSecurely123456";

    /*
     * This method creates a brand new token for a user who just logged in
     *
     * Steps:
     * 1. Put the user's email inside the token (so we know who it belongs to)
     * 2. Set when it was created (now)
     * 3. Set when it expires (24 hours from now)
     * 4. Sign it with our secret key so it can't be faked
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email) // store the user's email inside the token
                .issuedAt(new Date()) // mark the current time as "created at"
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // expires in 24 hours
                .signWith(getSignInKey()) // sign it so it can't be tampered with
                .compact(); // turn it into the final token string
    }

    /*
     * This method reads a token and pulls out the email
     * stored inside it
     *
     * Used later when checking "who is making this request?"
     */
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /*
     * This is a helper method that converts our secret key text
     * into the proper secure key object the JWT library needs
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY); // turn text into raw bytes
        return Keys.hmacShaKeyFor(keyBytes); // build a proper secure key object from those bytes
    }
}