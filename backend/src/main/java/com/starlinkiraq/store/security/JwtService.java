package com.starlinkiraq.store.security;

import com.starlinkiraq.store.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

/**
 * مسؤول عن إصدار والتحقق من رموز JWT (access token و refresh token).
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final AppProperties appProperties;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * يُنشئ access token قصير العمر يحمل هوية المستخدم ودوره.
     *
     * @param userId معرّف المستخدم
     * @param email البريد الإلكتروني (subject)
     * @param role دور المستخدم (CUSTOMER/ADMIN)
     * @return نص التوكن الموقّع
     */
    public String generateAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + appProperties.getJwt().getAccessTokenExpirationMs());
        return Jwts.builder()
                .issuer(appProperties.getJwt().getIssuer())
                .subject(email)
                .claim("userId", userId)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    /**
     * يُنشئ refresh token طويل العمر يُخزَّن كـ httpOnly cookie فقط.
     *
     * @param userId معرّف المستخدم
     * @param email البريد الإلكتروني (subject)
     * @return نص التوكن الموقّع
     */
    public String generateRefreshToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + appProperties.getJwt().getRefreshTokenExpirationMs());
        return Jwts.builder()
                .issuer(appProperties.getJwt().getIssuer())
                .subject(email)
                .claim("userId", userId)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .id(generateJti())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    private String generateJti() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * يستخرج الـ claims من توكن ويتحقق من توقيعه وصلاحيته.
     *
     * @param token نص التوكن
     * @return الـ claims المستخرجة
     * ملاحظة: يرمي JwtException إذا كان التوكن غير صالح أو منتهي الصلاحية
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * يتحقق من صحة التوكن (توقيع، صلاحية، وأنه ليس منتهياً).
     *
     * @param token نص التوكن
     * @return true إذا كان التوكن صالحاً
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshToken(Claims claims) {
        return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
    }

    public boolean isAccessToken(Claims claims) {
        return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
    }

    public String extractEmail(Claims claims) {
        return claims.getSubject();
    }

    public Long extractUserId(Claims claims) {
        return claims.get("userId", Long.class);
    }

    public Date extractExpiration(Claims claims) {
        return claims.getExpiration();
    }
}
