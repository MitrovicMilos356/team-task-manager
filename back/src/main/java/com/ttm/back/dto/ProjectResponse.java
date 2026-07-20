package com.ttm.back.dto;

import com.ttm.back.model.Project;

public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private boolean active;
    private long memberCount;
    private long openTaskCount;

    public static ProjectResponse fromEntity(Project project, long memberCount, long openTaskCount) {
        ProjectResponse response = new ProjectResponse();
        response.id = project.getId();
        response.name = project.getName();
        response.description = project.getDescription();
        response.active = project.isActive();
        response.memberCount = memberCount;
        response.openTaskCount = openTaskCount;
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
}
