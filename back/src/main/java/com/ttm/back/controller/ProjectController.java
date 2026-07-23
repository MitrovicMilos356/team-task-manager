package com.ttm.back.controller;

import com.ttm.back.dto.ProjectRequest;
import com.ttm.back.dto.ProjectResponse;
import com.ttm.back.security.CurrentUser;
import com.ttm.back.service.ProjectService;
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
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public Page<ProjectResponse> list(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return projectService.listProjects(pageable);
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        CurrentUser.requireAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        List<ProjectResponse> projects = projectService.exportProjects();
        List<String> headers = List.of("id", "name", "description", "active", "memberCount", "openTaskCount", "createdAt", "updatedAt");
        return CsvExporter.export("projects.csv", headers, projects, project -> List.of(
                String.valueOf(project.getId()),
                project.getName(),
                project.getDescription() != null ? project.getDescription() : "",
                String.valueOf(project.isActive()),
                String.valueOf(project.getMemberCount()),
                String.valueOf(project.getOpenTaskCount()),
                String.valueOf(project.getCreatedAt()),
                String.valueOf(project.getUpdatedAt())
        ));
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
