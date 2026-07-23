package com.ttm.back.dto;

import com.ttm.back.model.AuditLog;

import java.time.LocalDateTime;

public class AuditLogResponse {

    private Long id;
    private UserResponse actor;
    private String action;
    private String entityType;
    private Long entityId;
    private String details;
    private LocalDateTime createdAt;

    public static AuditLogResponse fromEntity(AuditLog entry, UserResponse actor) {
        AuditLogResponse response = new AuditLogResponse();
        response.id = entry.getId();
        response.actor = actor;
        response.action = entry.getAction();
        response.entityType = entry.getEntityType();
        response.entityId = entry.getEntityId();
        response.details = entry.getDetails();
        response.createdAt = entry.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public UserResponse getActor() {
        return actor;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
