package com.ttm.back.dto;

public class UpdateAssigneeRequest {

    private Long assignedUserId;

    public Long getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(Long assignedUserId) {
        this.assignedUserId = assignedUserId;
    }
}
