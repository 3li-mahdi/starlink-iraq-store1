package com.starlinkiraq.store.service;

import com.starlinkiraq.store.config.AppProperties;
import com.starlinkiraq.store.dto.auth.AuthResult;
import com.starlinkiraq.store.dto.auth.LoginRequest;
import com.starlinkiraq.store.dto.auth.RegisterRequest;
import com.starlinkiraq.store.entity.Role;
import com.starlinkiraq.store.entity.User;
import com.starlinkiraq.store.exception.AccountLockedException;
import com.starlinkiraq.store.exception.ConflictException;
import com.starlinkiraq.store.exception.UnauthorizedException;
import com.starlinkiraq.store.repository.EmailVerificationTokenRepository;
import com.starlinkiraq.store.repository.RefreshTokenRepository;
import com.starlinkiraq.store.repository.UserRepository;
import com.starlinkiraq.store.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * اختبارات وحدة لخدمة المصادقة: التسجيل، تسجيل الدخول، وقفل الحساب بعد محاولات فاشلة.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private EmailService emailService;

    private AppProperties appProperties;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        appProperties.getJwt().setSecret("test-secret-key-for-junit-tests-only-not-for-production-use-1234567890");
        appProperties.getJwt().setAccessTokenExpirationMs(900000);
        appProperties.getJwt().setRefreshTokenExpirationMs(604800000);
        appProperties.getJwt().setIssuer("test-issuer");
        appProperties.getSecurity().setMaxFailedLoginAttempts(5);
        appProperties.getSecurity().setAccountLockDurationMinutes(15);

        authService = new AuthService(userRepository, refreshTokenRepository, emailVerificationTokenRepository,
                passwordEncoder, jwtService, appProperties, auditLogService, emailService);
    }

    private void stubTokenIssuance() {
        Claims claims = mock(Claims.class);
        when(jwtService.generateAccessToken(any(), anyString(), anyString())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), anyString())).thenReturn("refresh-token");
        when(jwtService.parseClaims(anyString())).thenReturn(claims);
    }

    private User buildUser(String email, String encodedPassword) {
        return User.builder()
                .id(1L)
                .fullName("محمد أحمد")
                .email(email)
                .password(encodedPassword)
                .role(Role.CUSTOMER)
                .isEmailVerified(false)
                .failedLoginAttempts(0)
                .build();
    }

    @Test
    void register_shouldThrowConflict_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("محمد أحمد", "existing@example.com", "Password123", null);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request, "127.0.0.1"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void register_shouldCreateUserAndIssueTokens_whenEmailIsNew() {
        stubTokenIssuance();
        RegisterRequest request = new RegisterRequest("محمد أحمد", "new@example.com", "Password123", "07701234567");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        AuthResult result = authService.register(request, "127.0.0.1");

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.user().email()).isEqualTo("new@example.com");
        verify(emailService, times(1)).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void login_shouldThrowUnauthorized_whenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        LoginRequest request = new LoginRequest("missing@example.com", "whatever");

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(UnauthorizedException.class);
        verify(auditLogService).log(null, "LOGIN_FAILED", "User", null, "127.0.0.1");
    }

    @Test
    void login_shouldThrowAccountLocked_whenAccountCurrentlyLocked() {
        User user = buildUser("locked@example.com", "encoded");
        user.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findByEmail("locked@example.com")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("locked@example.com", "Password123");

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    void login_shouldLockAccount_afterMaxFailedAttempts() {
        User user = buildUser("bruteforce@example.com", "encoded");
        user.setFailedLoginAttempts(4);
        when(userRepository.findByEmail("bruteforce@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginRequest request = new LoginRequest("bruteforce@example.com", "WrongPassword");

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(UnauthorizedException.class);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(user.isAccountLocked()).isTrue();
    }

    @Test
    void login_shouldSucceedAndResetFailedAttempts_whenPasswordCorrect() {
        stubTokenIssuance();
        User user = buildUser("valid@example.com", "encoded");
        user.setFailedLoginAttempts(3);
        when(userRepository.findByEmail("valid@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "encoded")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginRequest request = new LoginRequest("valid@example.com", "Password123");
        AuthResult result = authService.login(request, "127.0.0.1");

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(user.getFailedLoginAttempts()).isZero();
    }
}
