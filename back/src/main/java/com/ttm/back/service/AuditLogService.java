package com.ttm.back.service;

import com.ttm.back.dto.AuditLogResponse;
import com.ttm.back.dto.UserResponse;
import com.ttm.back.model.AuditLog;
import com.ttm.back.model.User;
import com.ttm.back.repository.AuditLogRepository;
import com.ttm.back.repository.UserRepository;
import com.ttm.back.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public void record(String action, String entityType, Long entityId, String details) {
        AuditLog entry = new AuditLog();
        entry.setActorUserId(CurrentUser.get());
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setDetails(details);
        auditLogRepository.save(entry);
    }

    public Page<AuditLogResponse> list(Pageable pageable) {
        Page<AuditLog> entries = auditLogRepository.findAll(pageable);
        Map<Long, User> actorsById = loadUsers(entries.getContent().stream().map(AuditLog::getActorUserId).distinct().toList());
        return entries.map(entry -> AuditLogResponse.fromEntity(entry, UserResponse.fromEntity(actorsById.get(entry.getActorUserId()))));
    }

    private Map<Long, User> loadUsers(List<Long> ids) {
        Map<Long, User> byId = new HashMap<>();
        userRepository.findAllById(ids).forEach(u -> byId.put(u.getId(), u));
        return byId;
    }
}
