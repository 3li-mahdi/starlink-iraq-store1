package com.starlinkiraq.store.service;

import com.starlinkiraq.store.dto.common.AuditLogResponse;
import com.starlinkiraq.store.entity.AuditLog;
import com.starlinkiraq.store.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * يسجّل العمليات الحساسة بالنظام (دخول فاشل، دفع، تعديل إداري) لأغراض المراجعة الأمنية.
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * يسجّل حدثاً حساساً في سجل التدقيق.
     *
     * @param userId معرّف المستخدم المرتبط بالحدث (قد يكون null)
     * @param action نوع العملية المنفَّذة (مثل LOGIN_FAILED، ORDER_PLACED)
     * @param entityType نوع الكيان المتأثر (مثل User، Order)
     * @param entityId معرّف الكيان المتأثر (قد يكون null)
     * @param ipAddress عنوان IP الذي صدر منه الطلب
     * تأثير جانبي: يضيف سجلاً جديداً بجدول audit_logs، بمعاملة منفصلة حتى لا يفشل مع تراجع العملية الأصلية
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String action, String entityType, Long entityId, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }

    /**
     * يجلب سجلات التدقيق مرتبة من الأحدث للأقدم لعرضها بلوحة تحكم الأدمن.
     *
     * @param pageable معلومات الصفحة والحجم
     * @return صفحة من سجلات التدقيق
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable).map(AuditLogResponse::from);
    }
}
