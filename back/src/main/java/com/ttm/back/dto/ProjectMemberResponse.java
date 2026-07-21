package com.ttm.back.dto;

import java.time.LocalDateTime;

public class ProjectMemberResponse {

    private UserResponse user;
    private LocalDateTime joinedAt;

    public static ProjectMemberResponse of(UserResponse user, LocalDateTime joinedAt) {
        ProjectMemberResponse response = new ProjectMemberResponse();
        response.user = user;
        response.joinedAt = joinedAt;
        return response;
    }

    public UserResponse getUser() {
        return user;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
