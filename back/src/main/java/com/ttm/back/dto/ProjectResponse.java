package com.ttm.back.dto;

import com.ttm.back.model.Project;

import java.time.LocalDateTime;

public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private boolean active;
    private long memberCount;
    private long openTaskCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProjectResponse fromEntity(Project project, long memberCount, long openTaskCount) {
        ProjectResponse response = new ProjectResponse();
        response.id = project.getId();
        response.name = project.getName();
        response.description = project.getDescription();
        response.active = project.isActive();
        response.memberCount = memberCount;
        response.openTaskCount = openTaskCount;
        response.createdAt = project.getCreatedAt();
        response.updatedAt = project.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public long getMemberCount() {
        return memberCount;
    }

    public long getOpenTaskCount() {
        return openTaskCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
