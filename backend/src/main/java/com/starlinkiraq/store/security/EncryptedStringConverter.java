package com.starlinkiraq.store.security;

import com.starlinkiraq.store.config.AppProperties;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * يشفّر ويفكّ تشفير الأعمدة الحساسة بقاعدة البيانات (مثل محتوى DigitalAsset) باستخدام AES-256-GCM.
 * المفتاح يُشتق من app.encryption.secret-key عبر SHA-256 لضمان طول ثابت 256-bit.
 */
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private static AppProperties appProperties;

    @Autowired
    public void setAppProperties(AppProperties properties) {
        EncryptedStringConverter.appProperties = properties;
    }

    /**
     * يشفّر النص الأصلي قبل حفظه بقاعدة البيانات.
     *
     * @param attribute النص الأصلي غير المشفّر (قد يكون null)
     * @return النص بعد التشفير بصيغة Base64، أو null إذا كان المدخل null
     * تأثير جانبي: لا يوجد، دالة نقية تُستدعى تلقائياً من Hibernate عند الحفظ
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, deriveKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("فشل تشفير البيانات الحساسة", e);
        }
    }

    /**
     * يفكّ تشفير النص المخزَّن بقاعدة البيانات عند قراءته.
     *
     * @param dbData النص المشفّر المخزَّن (قد يكون null)
     * @return النص الأصلي بعد فك التشفير، أو null إذا كان المدخل null
     * تأثير جانبي: لا يوجد، دالة نقية تُستدعى تلقائياً من Hibernate عند القراءة
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(dbData);
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            byte[] cipherText = new byte[combined.length - GCM_IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("فشل فك تشفير البيانات الحساسة", e);
        }
    }

    private SecretKeySpec deriveKey() throws Exception {
        String secret = appProperties.getEncryption().getSecretKey();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }
}
