package com.starlinkiraq.store.security;

import com.starlinkiraq.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * يجلب بيانات المستخدم من قاعدة البيانات لأغراض المصادقة عبر Spring Security.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * يبحث عن مستخدم عبر بريده الإلكتروني ويحوّله إلى UserDetails.
     *
     * @param email البريد الإلكتروني المستخدم كاسم دخول
     * @return UserDetails يمثل المستخدم المطابق
     * ملاحظة: يرمي استثناء إذا لم يُوجد المستخدم، بدون كشف أي تفاصيل إضافية
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("بيانات الدخول غير صحيحة"));
    }
}
