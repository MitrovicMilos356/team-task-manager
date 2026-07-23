package com.ttm.back.controller;

import com.ttm.back.dto.CreateUserRequest;
import com.ttm.back.dto.UpdateStatusRequest;
import com.ttm.back.dto.UpdateUserRequest;
import com.ttm.back.dto.UserResponse;
import com.ttm.back.security.CurrentUser;
import com.ttm.back.service.AuthService;
import com.ttm.back.service.UserAdminService;
import com.ttm.back.util.CsvExporter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;
    private final UserAdminService userAdminService;

    public UserController(AuthService authService, UserAdminService userAdminService) {
        this.authService = authService;
        this.userAdminService = userAdminService;
    }

    @GetMapping("/me")
    public UserResponse me() {
        return authService.getById(CurrentUser.get());
    }

    @GetMapping
    public Page<UserResponse> list(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        CurrentUser.requireAdmin();
        return userAdminService.listUsers(pageable);
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        CurrentUser.requireAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(userAdminService.createUser(request));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        CurrentUser.requireAdmin();
        List<UserResponse> users = userAdminService.exportUsers();
        List<String> headers = List.of("id", "firstName", "lastName", "email", "role", "active", "createdAt", "updatedAt");
        return CsvExporter.export("users.csv", headers, users, user -> List.of(
                String.valueOf(user.getId()),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                String.valueOf(user.isActive()),
                String.valueOf(user.getCreatedAt()),
                String.valueOf(user.getUpdatedAt())
        ));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        CurrentUser.requireAdmin();
        return userAdminService.updateUser(id, request);
    }

    @PatchMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        CurrentUser.requireAdmin();
        return userAdminService.updateStatus(id, request.getActive());
    }
}
