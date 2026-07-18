package com.starlinkiraq.store.controller;

import com.starlinkiraq.store.dto.common.AuditLogResponse;
import com.starlinkiraq.store.dto.common.PageResponse;
import com.starlinkiraq.store.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * استعراض سجلات التدقيق الأمني (audit logs)، متاح للأدمن فقط.
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Audit Logs", description = "سجلات التدقيق الأمني (للأدمن فقط)")
public class AdminAuditLogController {

    private final AuditLogService auditLogService;

    @Operation(summary = "جلب سجلات التدقيق مرتبة من الأحدث للأقدم")
    @GetMapping
    public ResponseEntity<PageResponse<AuditLogResponse>> getAuditLogs(@PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(auditLogService.getAuditLogs(pageable)));
    }
}
