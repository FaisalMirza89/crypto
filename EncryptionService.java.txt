package com.app.service;

import org.springframework.stereotype.Service;

@Service
public class EncryptionService {
    
    private static final byte KEY = 0x5A; // Simple XOR key

    public byte[] encrypt(byte[] data) {
        return xor(data);
    }

    public byte[] decrypt(byte[] data) {
        return xor(data); // same as encrypt in XOR
    }

    private byte[] xor(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte)(data[i] ^ KEY);
        }
        return result;
    }
}
