package com.ttm.back.dto;

import jakarta.validation.constraints.NotNull;

public class AddMemberRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
