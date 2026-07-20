package com.ttm.back.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttm.back.model.Role;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthFilter extends HttpFilter {

    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/api/auth/",
            "/swagger-ui",
            "/v3/api-docs"
    );

    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String uri = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                || !uri.startsWith("/api/") || PUBLIC_PREFIXES.stream().anyMatch(uri::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            respondUnauthorized(response, "Missing or invalid Authorization header");
            return;
        }

        try {
            var claims = jwtService.parseClaims(header.substring(7));
            Long userId = claims.get("userId", Long.class);
            Role role = Role.valueOf(claims.get("role", String.class));
            CurrentUser.set(userId, role);
            chain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException e) {
            respondUnauthorized(response, "Invalid or expired token");
        } finally {
            CurrentUser.clear();
        }
    }

    private void respondUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", message)));
    }
}
