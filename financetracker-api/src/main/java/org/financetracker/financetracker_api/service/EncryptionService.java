package org.financetracker.financetracker_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/*
 * EncryptionService — THE LOCKSMITH
 *
 * This service has two jobs:
 * 1. encrypt() — scrambles plain text into unreadable ciphertext
 * 2. decrypt() — unscrambles ciphertext back into plain text
 *
 * It uses AES-256, the industry standard encryption algorithm.
 * AES needs a secret key — we store that key in Railway as
 * an environment variable called ENCRYPTION_KEY so it never
 * appears in our code or on GitHub.
 *
 * Think of it like this:
 * encrypt("access-sandbox-abc123") → "aGVsbG8gd29ybGQ..."
 * decrypt("aGVsbG8gd29ybGQ...") → "access-sandbox-abc123"
 */
@Service
public class EncryptionService {

    // reads ENCRYPTION_KEY from Railway environment variables
    // same pattern as PLAID_CLIENT_ID and PLAID_SECRET
    @Value("${ENCRYPTION_KEY}")
    private String encryptionKey;

    // AES = Advanced Encryption Standard
    // the industry standard symmetric encryption algorithm
    private static final String ALGORITHM = "AES";

    /*
     * Takes plain text and returns scrambled ciphertext
     * Called automatically before saving accessToken to DB
     *
     * Steps:
     * 1. Build the AES key from our secret string
     * 2. Create an AES cipher in encrypt mode
     * 3. Scramble the plain text bytes
     * 4. Encode result as Base64 so it's safe to store as a String
     */
    public String encrypt(String plainText) {
        try {
            // build the AES key from our secret string
            // getBytes() converts String to byte array
            SecretKeySpec keySpec = new SecretKeySpec(
                    encryptionKey.getBytes(), // our secret key as bytes
                    ALGORITHM                 // "AES"
            );

            // create an AES cipher ready to encrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            // scramble the plain text into encrypted bytes
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());

            // encode to Base64 so we can store it as a normal String in the DB
            // Base64 converts raw bytes into a safe text format
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {
            // if encryption fails, throw a clear error
            throw new RuntimeException("Error encrypting value", e);
        }
    }

    /*
     * Takes scrambled ciphertext and returns plain text
     * Called automatically after reading accessToken from DB
     *
     * Steps:
     * 1. Build the same AES key from our secret string
     * 2. Create an AES cipher in decrypt mode
     * 3. Decode from Base64 back to bytes
     * 4. Unscramble back to the original plain text
     */
    public String decrypt(String cipherText) {
        try {
            // build the exact same key — must match what we used to encrypt
            SecretKeySpec keySpec = new SecretKeySpec(
                    encryptionKey.getBytes(),
                    ALGORITHM
            );

            // create an AES cipher ready to decrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            // decode from Base64 back to encrypted bytes
            byte[] encryptedBytes = Base64.getDecoder().decode(cipherText);

            // unscramble back to the original plain text bytes
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            // convert bytes back to String and return
            return new String(decryptedBytes);

        } catch (Exception e) {
            throw new RuntimeException("Error decrypting value", e);
        }
    }
}