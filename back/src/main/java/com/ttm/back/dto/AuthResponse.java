package com.ttm.back.dto;

public class AuthResponse {

    private final String token;
    private final String refreshToken;
    private final UserResponse user;

    public AuthResponse(String token, String refreshToken, UserResponse user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public UserResponse getUser() {
        return user;
    }
}
