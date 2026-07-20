package com.ttm.back.security;

import com.ttm.back.exception.ApiException;
import com.ttm.back.model.Role;
import org.springframework.http.HttpStatus;

public final class CurrentUser {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Role> ROLE = new ThreadLocal<>();

    private CurrentUser() {
    }

    public static void set(Long userId, Role role) {
        USER_ID.set(userId);
        ROLE.set(role);
    }

    public static Long get() {
        return USER_ID.get();
    }

    public static Role getRole() {
        return ROLE.get();
    }

    public static void requireAdmin() {
        if (ROLE.get() != Role.admin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}
