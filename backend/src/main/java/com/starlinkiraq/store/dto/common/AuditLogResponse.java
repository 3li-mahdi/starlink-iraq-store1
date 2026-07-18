package com.starlinkiraq.store.dto.common;

import com.starlinkiraq.store.entity.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long userId,
        String action,
        String entityType,
        Long entityId,
        String ipAddress,
        LocalDateTime timestamp
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getUserId(), log.getAction(), log.getEntityType(),
                log.getEntityId(), log.getIpAddress(), log.getTimestamp());
    }
}
