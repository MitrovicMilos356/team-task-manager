package com.ttm.back.service;

import com.ttm.back.dto.CreateTaskRequest;
import com.ttm.back.dto.TaskResponse;
import com.ttm.back.dto.UserResponse;
import com.ttm.back.exception.ApiException;
import com.ttm.back.model.Project;
import com.ttm.back.model.Task;
import com.ttm.back.model.TaskStatus;
import com.ttm.back.model.User;
import com.ttm.back.repository.ProjectMemberRepository;
import com.ttm.back.repository.ProjectRepository;
import com.ttm.back.repository.TaskRepository;
import com.ttm.back.repository.UserRepository;
import com.ttm.back.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
                        ProjectRepository projectRepository,
                        ProjectMemberRepository projectMemberRepository,
                        UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    public List<TaskResponse> listByProject(Long projectId) {
        findProjectOrThrow(projectId);
        List<Task> tasks = taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        Map<Long, User> assigneesById = loadAssignees(tasks);
        return tasks.stream()
                .map(task -> toResponse(task, assigneesById))
                .toList();
    }

    public TaskResponse createTask(CreateTaskRequest request) {
        Project project = findProjectOrThrow(request.getProjectId());
        if (!project.isActive()) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot create tasks in an inactive project");
        }

        if (request.getAssignedUserId() != null) {
            requireProjectMember(request.getProjectId(), request.getAssignedUserId());
        }

        Task task = new Task();
        task.setProjectId(request.getProjectId());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.todo);
        task.setPriority(request.getPriority());
        task.setAssignedUserId(request.getAssignedUserId());
        task.setCreatedByUserId(CurrentUser.get());
        task.setDueDate(request.getDueDate());

        Task saved = taskRepository.save(task);
        UserResponse assignee = saved.getAssignedUserId() != null
                ? UserResponse.fromEntity(userRepository.findById(saved.getAssignedUserId()).orElse(null))
                : null;
        return TaskResponse.fromEntity(saved, assignee);
    }

    public TaskResponse updateAssignee(Long taskId, Long assignedUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));

        if (assignedUserId != null) {
            requireProjectMember(task.getProjectId(), assignedUserId);
        }

        task.setAssignedUserId(assignedUserId);
        Task saved = taskRepository.save(task);
        UserResponse assignee = assignedUserId != null
                ? UserResponse.fromEntity(userRepository.findById(assignedUserId).orElse(null))
                : null;
        return TaskResponse.fromEntity(saved, assignee);
    }

    private void requireProjectMember(Long projectId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Assignee must be a member of the project");
        }
    }

    private Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private Map<Long, User> loadAssignees(List<Task> tasks) {
        List<Long> ids = tasks.stream()
                .map(Task::getAssignedUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> byId = new HashMap<>();
        userRepository.findAllById(ids).forEach(u -> byId.put(u.getId(), u));
        return byId;
    }

    private TaskResponse toResponse(Task task, Map<Long, User> assigneesById) {
        UserResponse assignee = task.getAssignedUserId() != null
                ? UserResponse.fromEntity(assigneesById.get(task.getAssignedUserId()))
                : null;
        return TaskResponse.fromEntity(task, assignee);
    }
}
