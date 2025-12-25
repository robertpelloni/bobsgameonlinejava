package com.bobsgame.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.bobsgame.net.PrivateCredentials;
import com.bobsgame.shared.Utils;

public class PasswordManagerTest {

    @Test
    public void testBcryptHashing() {
        String password = "password123";
        String hash = PasswordManager.hashPassword(password);

        // Bcrypt hashes usually start with $2a$ and are 60 chars long
        assertTrue(hash.length() == 60);
        assertTrue(PasswordManager.checkPassword(password, hash, 0));
        assertFalse(PasswordManager.checkPassword("wrongpassword", hash, 0));
        assertFalse(PasswordManager.isLegacy(hash));
    }

    @Test
    public void testLegacyMD5Hashing() {
        String password = "password123";
        long creationTime = 123456789L;
        // Replicate legacy logic: Utils.getStringMD5(PrivateCredentials.passwordSalt + password + accountCreatedTime)
        String legacyHash = Utils.getStringMD5(PrivateCredentials.passwordSalt + password + creationTime);

        assertTrue(PasswordManager.isLegacy(legacyHash));
        assertTrue(PasswordManager.checkPassword(password, legacyHash, creationTime));
        assertFalse(PasswordManager.checkPassword("wrongpassword", legacyHash, creationTime));
    }
}
