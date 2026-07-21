package com.ttm.back.controller;

import com.ttm.back.dto.ProjectRequest;
import com.ttm.back.dto.ProjectResponse;
import com.ttm.back.security.CurrentUser;
import com.ttm.back.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> list() {
        return projectService.listProjects();
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        CurrentUser.requireAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable Long id) {
        return projectService.getProject(id);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        CurrentUser.requireAdmin();
        return projectService.updateProject(id, request);
    }

    @DeleteMapping("/{id}")
    public ProjectResponse deactivate(@PathVariable Long id) {
        CurrentUser.requireAdmin();
        return projectService.deactivateProject(id);
    }
}
