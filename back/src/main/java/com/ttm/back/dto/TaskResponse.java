package com.ttm.back.dto;

import com.ttm.back.model.Task;
import com.ttm.back.model.TaskPriority;
import com.ttm.back.model.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskResponse {

    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private UserResponse assignedUser;
    private Long createdByUserId;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse fromEntity(Task task, UserResponse assignedUser) {
        TaskResponse response = new TaskResponse();
        response.id = task.getId();
        response.projectId = task.getProjectId();
        response.title = task.getTitle();
        response.description = task.getDescription();
        response.status = task.getStatus();
        response.priority = task.getPriority();
        response.assignedUser = assignedUser;
        response.createdByUserId = task.getCreatedByUserId();
        response.dueDate = task.getDueDate();
        response.createdAt = task.getCreatedAt();
        response.updatedAt = task.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public UserResponse getAssignedUser() {
        return assignedUser;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
