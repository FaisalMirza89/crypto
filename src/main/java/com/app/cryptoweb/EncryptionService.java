package com.app.cryptoweb;

import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.util.Arrays;
import java.security.spec.KeySpec;


@Service
public class EncryptionService {

    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;
    private static final int KEY_LENGTH = 256;
    private static final int ITERATIONS = 65536;

    public byte[] encrypt(byte[] data, String password) throws Exception {
        // Generate salt and IV
        byte[] salt = generateRandomBytes(SALT_LENGTH);
        byte[] iv = generateRandomBytes(IV_LENGTH);

        SecretKey key = getKeyFromPassword(password, salt);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(salt);      // 0 - 15
        outputStream.write(iv);        // 16 - 31
        outputStream.write(encrypted); // 32+

        return outputStream.toByteArray();
    }

    public byte[] decrypt(byte[] encryptedData, String password) throws Exception {
        if (encryptedData.length < SALT_LENGTH + IV_LENGTH) {
            throw new IllegalArgumentException("Invalid encrypted data.");
        }

        byte[] salt = Arrays.copyOfRange(encryptedData, 0, SALT_LENGTH);
        byte[] iv = Arrays.copyOfRange(encryptedData, SALT_LENGTH, SALT_LENGTH + IV_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(encryptedData, SALT_LENGTH + IV_LENGTH, encryptedData.length);

        SecretKey key = getKeyFromPassword(password, salt);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(ciphertext);
    }

    private SecretKey getKeyFromPassword(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
