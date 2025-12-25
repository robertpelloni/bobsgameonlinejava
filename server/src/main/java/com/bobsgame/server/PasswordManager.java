package com.bobsgame.server;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.bobsgame.net.PrivateCredentials;
import com.bobsgame.shared.Utils;

public class PasswordManager {

    // Cost factor for Bcrypt (log2 iterations). 12 is a good modern default.
    private static final int COST_FACTOR = 12;

    /**
     * Hashes a password using Bcrypt.
     */
    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(COST_FACTOR, password.toCharArray());
    }

    /**
     * Checks a password against a stored hash.
     * Handles legacy MD5 hashes by checking format and verifying using legacy logic.
     *
     * @param password The plain text password.
     * @param storedHash The hash stored in the database.
     * @param accountCreatedTime The account creation timestamp (legacy salt).
     * @return true if password matches.
     */
    public static boolean checkPassword(String password, String storedHash, long accountCreatedTime) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }

        // Check if it's a legacy MD5 hash (MD5 hex string is 32 chars)
        // Bcrypt hashes start with $2a$, $2b$, or $2y$ and are 60 chars long.
        if (storedHash.length() == 32) {
            // Legacy check
            String legacyHash = Utils.getStringMD5(PrivateCredentials.passwordSalt + password + accountCreatedTime);
            return storedHash.equalsIgnoreCase(legacyHash);
        }

        // Bcrypt check
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), storedHash);
        return result.verified;
    }

    /**
     * Checks if the stored hash is a legacy hash that needs upgrading.
     */
    public static boolean isLegacy(String storedHash) {
        return storedHash != null && storedHash.length() == 32;
    }
}
