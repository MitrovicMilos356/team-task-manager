package com.ttm.back.service;

import com.ttm.back.dto.DashboardStatsResponse;
import com.ttm.back.model.Role;
import com.ttm.back.model.Task;
import com.ttm.back.model.TaskStatus;
import com.ttm.back.repository.ProjectRepository;
import com.ttm.back.repository.TaskRepository;
import com.ttm.back.security.CurrentUser;
import com.ttm.back.security.ProjectAccessGuard;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class DashboardService {

    private static final Set<TaskStatus> CLOSED_STATUSES = Set.of(TaskStatus.done, TaskStatus.canceled);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAccessGuard accessGuard;

    public DashboardService(TaskRepository taskRepository, ProjectRepository projectRepository, ProjectAccessGuard accessGuard) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.accessGuard = accessGuard;
    }

    public DashboardStatsResponse getStatistics() {
        boolean isAdmin = CurrentUser.getRole() == Role.admin;

        long totalProjects;
        List<Task> tasks;
        if (isAdmin) {
            totalProjects = projectRepository.count();
            tasks = taskRepository.findAll();
        } else {
            List<Long> visibleProjectIds = accessGuard.visibleProjectIds();
            totalProjects = visibleProjectIds.size();
            tasks = visibleProjectIds.isEmpty() ? List.of() : taskRepository.findByProjectIdIn(visibleProjectIds);
        }

        Map<TaskStatus, Long> tasksByStatus = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            tasksByStatus.put(status, 0L);
        }
        for (Task task : tasks) {
            tasksByStatus.merge(task.getStatus(), 1L, Long::sum);
        }

        long totalOpenTasks = tasks.stream().filter(t -> !CLOSED_STATUSES.contains(t.getStatus())).count();

        LocalDate today = LocalDate.now();
        long overdueTasks = tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today) && !CLOSED_STATUSES.contains(t.getStatus()))
                .count();

        Long currentUserId = CurrentUser.get();
        long myAssignedOpenTasks = tasks.stream()
                .filter(t -> Objects.equals(t.getAssignedUserId(), currentUserId) && !CLOSED_STATUSES.contains(t.getStatus()))
                .count();

        return new DashboardStatsResponse(totalProjects, totalOpenTasks, tasksByStatus, overdueTasks, myAssignedOpenTasks);
    }
}
