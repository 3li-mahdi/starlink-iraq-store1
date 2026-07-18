package com.starlinkiraq.store.service;

import com.starlinkiraq.store.config.AppProperties;
import com.starlinkiraq.store.dto.auth.AuthResult;
import com.starlinkiraq.store.dto.auth.LoginRequest;
import com.starlinkiraq.store.dto.auth.RegisterRequest;
import com.starlinkiraq.store.dto.auth.UserResponse;
import com.starlinkiraq.store.entity.EmailVerificationToken;
import com.starlinkiraq.store.entity.RefreshToken;
import com.starlinkiraq.store.entity.Role;
import com.starlinkiraq.store.entity.User;
import com.starlinkiraq.store.exception.AccountLockedException;
import com.starlinkiraq.store.exception.BadRequestException;
import com.starlinkiraq.store.exception.ConflictException;
import com.starlinkiraq.store.exception.UnauthorizedException;
import com.starlinkiraq.store.repository.EmailVerificationTokenRepository;
import com.starlinkiraq.store.repository.RefreshTokenRepository;
import com.starlinkiraq.store.repository.UserRepository;
import com.starlinkiraq.store.security.JwtService;
import com.starlinkiraq.store.security.TokenHasher;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * يدير كامل دورة حياة المصادقة: التسجيل، تسجيل الدخول، تجديد التوكن، تسجيل الخروج، وتفعيل البريد الإلكتروني.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties appProperties;
    private final AuditLogService auditLogService;
    private final EmailService emailService;

    /**
     * يسجّل مستخدماً جديداً بدور CUSTOMER ويرسل له رابط تفعيل البريد الإلكتروني.
     *
     * @param request بيانات التسجيل (الاسم، البريد، كلمة المرور، الهاتف)
     * @param ipAddress عنوان IP لمصدر الطلب
     * @return نتيجة المصادقة مع access/refresh token وبيانات المستخدم
     * تأثير جانبي: يضيف مستخدماً جديداً بقاعدة البيانات ويرسل بريد تفعيل ويسجّل الحدث بسجل التدقيق
     */
    @Transactional
    public AuthResult register(RegisterRequest request, String ipAddress) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("البريد الإلكتروني مستخدم مسبقاً");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .role(Role.CUSTOMER)
                .isEmailVerified(false)
                .failedLoginAttempts(0)
                .build();
        user = userRepository.save(user);

        issueEmailVerificationToken(user);
        auditLogService.log(user.getId(), "REGISTER", "User", user.getId(), ipAddress);

        return issueTokens(user);
    }

    /**
     * يسجّل دخول مستخدم موجود بعد التحقق من كلمة المرور، مع تطبيق قفل الحساب بعد محاولات فاشلة متكررة.
     *
     * @param request بيانات الدخول (البريد وكلمة المرور)
     * @param ipAddress عنوان IP لمصدر الطلب
     * @return نتيجة المصادقة مع access/refresh token وبيانات المستخدم
     * تأثير جانبي: قد يعدّل عداد المحاولات الفاشلة أو يقفل الحساب، ويسجّل كل محاولة بسجل التدقيق
     */
    @Transactional
    public AuthResult login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByEmail(request.email()).orElse(null);

        if (user == null) {
            auditLogService.log(null, "LOGIN_FAILED", "User", null, ipAddress);
            throw new UnauthorizedException("بيانات الدخول غير صحيحة");
        }

        if (user.isAccountLocked()) {
            auditLogService.log(user.getId(), "LOGIN_BLOCKED_LOCKED", "User", user.getId(), ipAddress);
            throw new AccountLockedException("الحساب مقفل مؤقتاً بسبب محاولات دخول فاشلة متكررة، حاول لاحقاً");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            registerFailedAttempt(user);
            auditLogService.log(user.getId(), "LOGIN_FAILED", "User", user.getId(), ipAddress);
            throw new UnauthorizedException("بيانات الدخول غير صحيحة");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        auditLogService.log(user.getId(), "LOGIN_SUCCESS", "User", user.getId(), ipAddress);

        return issueTokens(user);
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= appProperties.getSecurity().getMaxFailedLoginAttempts()) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(appProperties.getSecurity().getAccountLockDurationMinutes()));
        }
        userRepository.save(user);
    }

    /**
     * يُصدر access token جديداً باستخدام refresh token صالح، مع تدوير (rotation) رمز التجديد لأمان أعلى.
     *
     * @param rawRefreshToken قيمة رمز التجديد المستلمة من كوكي httpOnly
     * @return نتيجة المصادقة مع access token جديد و refresh token جديد
     * تأثير جانبي: يُلغي رمز التجديد القديم ويصدر رمزاً جديداً بقاعدة البيانات
     */
    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("رمز التجديد مفقود");
        }
        if (!jwtService.isTokenValid(rawRefreshToken)) {
            throw new UnauthorizedException("رمز التجديد غير صالح أو منتهي الصلاحية");
        }

        Claims claims = jwtService.parseClaims(rawRefreshToken);
        if (!jwtService.isRefreshToken(claims)) {
            throw new UnauthorizedException("نوع الرمز غير صحيح");
        }

        String tokenHash = TokenHasher.hash(rawRefreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("رمز التجديد غير صالح"));

        if (!storedToken.isValid()) {
            throw new UnauthorizedException("رمز التجديد غير صالح أو تم إلغاؤه");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        return issueTokens(user);
    }

    /**
     * يُبطل رمز التجديد الحالي عند تسجيل الخروج.
     *
     * @param rawRefreshToken قيمة رمز التجديد المستلمة من كوكي httpOnly (قد تكون null)
     * @param userId معرّف المستخدم لتسجيل الحدث بسجل التدقيق (قد يكون null)
     * @param ipAddress عنوان IP لمصدر الطلب
     * تأثير جانبي: يُبطل رمز التجديد بقاعدة البيانات ويسجّل الحدث بسجل التدقيق
     */
    @Transactional
    public void logout(String rawRefreshToken, Long userId, String ipAddress) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String tokenHash = TokenHasher.hash(rawRefreshToken);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
        auditLogService.log(userId, "LOGOUT", "User", userId, ipAddress);
    }

    /**
     * يفعّل حساب المستخدم بعد التحقق من صحة رمز التفعيل المرسل بالبريد الإلكتروني.
     *
     * @param token رمز التفعيل
     * تأثير جانبي: يعدّل isEmailVerified للمستخدم إلى true ويحذف رمز التفعيل المستخدَم
     */
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("رمز التفعيل غير صحيح"));

        if (verificationToken.isExpired()) {
            throw new BadRequestException("انتهت صلاحية رمز التفعيل، الرجاء طلب رمز جديد");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        emailVerificationTokenRepository.delete(verificationToken);
    }

    private void issueEmailVerificationToken(User user) {
        String token = generateSecureToken();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    private AuthResult issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        Claims refreshClaims = jwtService.parseClaims(refreshToken);
        RefreshToken tokenEntity = RefreshToken.builder()
                .tokenHash(TokenHasher.hash(refreshToken))
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(appProperties.getJwt().getRefreshTokenExpirationMs() / 1000))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(tokenEntity);

        return new AuthResult(accessToken, refreshToken, UserResponse.from(user));
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
