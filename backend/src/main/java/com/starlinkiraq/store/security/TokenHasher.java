package com.starlinkiraq.store.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * يحوّل أي رمز (توكن) إلى بصمة SHA-256 قبل تخزينه بقاعدة البيانات، بحيث لا يُحفظ الرمز الخام أبداً.
 */
public final class TokenHasher {

    private TokenHasher() {
    }

    public static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("خوارزمية التشفير غير متوفرة", e);
        }
    }
}
