package org.financetracker.financetracker_api.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.financetracker.financetracker_api.service.EncryptionService;
import org.springframework.stereotype.Component;

/*
 * AccessTokenConverter — THE AUTOMATIC TRANSLATOR
 *
 * This class sits between JPA and the database.
 * It automatically encrypts before saving and decrypts after reading.
 *
 * JPA calls convertToDatabaseColumn() before every INSERT/UPDATE
 * JPA calls convertToEntityAttribute() after every SELECT
 *
 * Your PlaidService code doesn't change at all —
 * encryption and decryption are completely invisible to it.
 *
 * Think of it like a post office that automatically seals
 * every letter before sending and opens it on arrival —
 * the sender and receiver just deal with normal letters.
 *
 * AttributeConverter<X, Y> means:
 * X = the Java type (String — what your code sees)
 * Y = the DB type  (String — what gets stored in the DB)
 */
@Converter
@Component
public class AccessTokenConverter implements AttributeConverter<String, String> {

    // Spring injects our EncryptionService automatically
    private final EncryptionService encryptionService;

    public AccessTokenConverter(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    /*
     * Called by JPA BEFORE saving to the database
     * Encrypts the plain accessToken into scrambled ciphertext
     *
     * "convertToDatabaseColumn" = "what should we store in the DB?"
     */
    @Override
    public String convertToDatabaseColumn(String plainToken) {
        // if the token is null or empty, don't try to encrypt
        if (plainToken == null || plainToken.isEmpty()) {
            return plainToken;
        }
        // encrypt the plain token before storing
        return encryptionService.encrypt(plainToken);
    }

    /*
     * Called by JPA AFTER reading from the database
     * Decrypts the scrambled ciphertext back into the plain accessToken
     *
     * "convertToEntityAttribute" = "what should our Java code see?"
     */
    @Override
    public String convertToEntityAttribute(String encryptedToken) {
        // if the stored value is null or empty, return as is
        if (encryptedToken == null || encryptedToken.isEmpty()) {
            return encryptedToken;
        }
        // decrypt back to plain text so PlaidService can use it
        return encryptionService.decrypt(encryptedToken);
    }
}