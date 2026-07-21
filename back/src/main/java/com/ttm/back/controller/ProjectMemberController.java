package com.ttm.back.controller;

import com.ttm.back.dto.AddMemberRequest;
import com.ttm.back.dto.ProjectMemberResponse;
import com.ttm.back.security.CurrentUser;
import com.ttm.back.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectService projectService;

    public ProjectMemberController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectMemberResponse> list(@PathVariable Long projectId) {
        return projectService.listMembers(projectId);
    }

    @PostMapping
    public ResponseEntity<ProjectMemberResponse> add(@PathVariable Long projectId, @Valid @RequestBody AddMemberRequest request) {
        CurrentUser.requireAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.addMember(projectId, request.getUserId()));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> remove(@PathVariable Long projectId, @PathVariable Long userId) {
        CurrentUser.requireAdmin();
        projectService.removeMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }
}
