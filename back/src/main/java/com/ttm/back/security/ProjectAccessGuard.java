package com.ttm.back.security;

import com.ttm.back.exception.ApiException;
import com.ttm.back.model.ProjectMember;
import com.ttm.back.model.Role;
import com.ttm.back.repository.ProjectMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectAccessGuard {

    private final ProjectMemberRepository projectMemberRepository;

    public ProjectAccessGuard(ProjectMemberRepository projectMemberRepository) {
        this.projectMemberRepository = projectMemberRepository;
    }

    public boolean canView(Long projectId) {
        return CurrentUser.getRole() == Role.admin
                || projectMemberRepository.existsByProjectIdAndUserId(projectId, CurrentUser.get());
    }

    public void assertCanView(Long projectId) {
        if (!canView(projectId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have access to this project");
        }
    }

    public List<Long> visibleProjectIds() {
        return projectMemberRepository.findByUserId(CurrentUser.get()).stream()
                .map(ProjectMember::getProjectId)
                .toList();
    }
}
