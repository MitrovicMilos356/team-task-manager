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

    public ProjectService(ProjectRepository projectRepository,
                           ProjectMemberRepository projectMemberRepository,
                           UserRepository userRepository,
                           ProjectAccessGuard accessGuard) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.accessGuard = accessGuard;
    }

    public List<ProjectResponse> listProjects() {
        List<Project> projects = CurrentUser.getRole() == Role.admin
                ? projectRepository.findAll()
                : projectRepository.findAllById(accessGuard.visibleProjectIds());
        return projects.stream()
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
        return toResponse(projectRepository.save(project));
    }

    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = findProjectOrThrow(id);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        return toResponse(projectRepository.save(project));
    }

    public ProjectResponse deactivateProject(Long id) {
        Project project = findProjectOrThrow(id);
        project.setActive(false);
        return toResponse(projectRepository.save(project));
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

        return ProjectMemberResponse.of(UserResponse.fromEntity(user), saved.getCreatedAt());
    }

    public void removeMember(Long projectId, Long userId) {
        findProjectOrThrow(projectId);
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User is not a member of this project"));
        projectMemberRepository.delete(member);
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
