package com.ttm.back.service;

import com.ttm.back.dto.ProjectRequest;
import com.ttm.back.dto.ProjectResponse;
import com.ttm.back.dto.UserResponse;
import com.ttm.back.exception.ApiException;
import com.ttm.back.model.Project;
import com.ttm.back.model.ProjectMember;
import com.ttm.back.model.User;
import com.ttm.back.repository.ProjectMemberRepository;
import com.ttm.back.repository.ProjectRepository;
import com.ttm.back.repository.UserRepository;
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

    public ProjectService(ProjectRepository projectRepository,
                           ProjectMemberRepository projectMemberRepository,
                           UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    public List<ProjectResponse> listProjects() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getProject(Long id) {
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

    public List<UserResponse> listMembers(Long projectId) {
        findProjectOrThrow(projectId);
        List<Long> userIds = projectMemberRepository.findByProjectId(projectId).stream()
                .map(ProjectMember::getUserId)
                .toList();
        Map<Long, User> usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return userIds.stream()
                .map(usersById::get)
                .filter(Objects::nonNull)
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse addMember(Long projectId, Long userId) {
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
        projectMemberRepository.save(member);

        return UserResponse.fromEntity(user);
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
