package com.ttm.back.dto;

import com.ttm.back.model.TaskHistory;

import java.time.LocalDateTime;

public class TaskHistoryResponse {

    private Long id;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private UserResponse changedBy;
    private LocalDateTime createdAt;

    public static TaskHistoryResponse fromEntity(TaskHistory history, UserResponse changedBy) {
        TaskHistoryResponse response = new TaskHistoryResponse();
        response.id = history.getId();
        response.fieldName = history.getFieldName();
        response.oldValue = history.getOldValue();
        response.newValue = history.getNewValue();
        response.changedBy = changedBy;
        response.createdAt = history.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public UserResponse getChangedBy() {
        return changedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
