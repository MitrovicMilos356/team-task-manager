package com.ttm.back.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "Active flag is required")
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
