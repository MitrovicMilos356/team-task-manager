package com.ttm.back.service;

import com.ttm.back.dto.ProjectMemberResponse;
import com.ttm.back.dto.ProjectRequest;
import com.ttm.back.dto.ProjectResponse;
import com.ttm.back.dto.UserResponse;
import com.ttm.back.exception.ApiException;
import com.ttm.back.model.Project;
import com.ttm.back.model.ProjectMember;
import com.ttm.back.model.User;
import com.ttm.back.model.Role;
import com.ttm.back.repository.ProjectMemberRepository;
import com.ttm.back.repository.ProjectRepository;
import com.ttm.back.repository.UserRepository;
import com.ttm.back.security.CurrentUser;
import com.ttm.back.security.ProjectAccessGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectAccessGuard accessGuard;
    private final AuditLogService auditLogService;

    public ProjectService(ProjectRepository projectRepository,
                           ProjectMemberRepository projectMemberRepository,
                           UserRepository userRepository,
                           ProjectAccessGuard accessGuard,
                           AuditLogService auditLogService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.accessGuard = accessGuard;
        this.auditLogService = auditLogService;
    }

    public Page<ProjectResponse> listProjects(Pageable pageable) {
        if (CurrentUser.getRole() == Role.admin) {
            return projectRepository.findAll(pageable).map(this::toResponse);
        }

        List<ProjectResponse> visible = projectRepository.findAllById(accessGuard.visibleProjectIds()).stream()
                .map(this::toResponse)
                .toList();
        return sliceInMemory(visible, pageable);
    }

    private <T> Page<T> sliceInMemory(List<T> items, Pageable pageable) {
        int start = Math.min((int) pageable.getOffset(), items.size());
        int end = Math.min(start + pageable.getPageSize(), items.size());
        return new PageImpl<>(items.subList(start, end), pageable, items.size());
    }

    public List<ProjectResponse> exportProjects() {
        CurrentUser.requireAdmin();
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getProject(Long id) {
        accessGuard.assertCanView(id);
        return toResponse(findProjectOrThrow(id));
    }

    public ProjectResponse createProject(ProjectRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setActive(true);
        Project saved = projectRepository.save(project);
        auditLogService.record("PROJECT_CREATED", "PROJECT", saved.getId(), "name=" + saved.getName());
        return toResponse(saved);
    }

    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = findProjectOrThrow(id);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        Project saved = projectRepository.save(project);
        auditLogService.record("PROJECT_UPDATED", "PROJECT", saved.getId(), "name=" + saved.getName());
        return toResponse(saved);
    }

    public ProjectResponse deactivateProject(Long id) {
        Project project = findProjectOrThrow(id);
        project.setActive(false);
        Project saved = projectRepository.save(project);
        auditLogService.record("PROJECT_DEACTIVATED", "PROJECT", saved.getId(), "name=" + saved.getName());
        return toResponse(saved);
    }

    public List<ProjectMemberResponse> listMembers(Long projectId) {
        findProjectOrThrow(projectId);
        accessGuard.assertCanView(projectId);
        List<ProjectMember> memberships = projectMemberRepository.findByProjectId(projectId);
        Map<Long, User> usersById = userRepository.findAllById(memberships.stream().map(ProjectMember::getUserId).toList())
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return memberships.stream()
                .filter(m -> usersById.get(m.getUserId()) != null)
                .map(m -> ProjectMemberResponse.of(UserResponse.fromEntity(usersById.get(m.getUserId())), m.getCreatedAt()))
                .toList();
    }

    public ProjectMemberResponse addMember(Long projectId, Long userId) {
        Project project = findProjectOrThrow(projectId);
        if (!project.isActive()) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot add members to an inactive project");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "User is already a member of this project");
        }

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        ProjectMember saved = projectMemberRepository.save(member);

        auditLogService.record("PROJECT_MEMBER_ADDED", "PROJECT", projectId, "userId=" + userId);
        return ProjectMemberResponse.of(UserResponse.fromEntity(user), saved.getCreatedAt());
    }

    public void removeMember(Long projectId, Long userId) {
        findProjectOrThrow(projectId);
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User is not a member of this project"));
        projectMemberRepository.delete(member);
        auditLogService.record("PROJECT_MEMBER_REMOVED", "PROJECT", projectId, "userId=" + userId);
    }

    private Project findProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private ProjectResponse toResponse(Project project) {
        long memberCount = projectRepository.countMembers(project.getId());
        long openTaskCount = projectRepository.countOpenTasks(project.getId());
        return ProjectResponse.fromEntity(project, memberCount, openTaskCount);
    }
}
