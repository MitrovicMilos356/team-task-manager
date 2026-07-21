package com.ttm.back.service;

import com.ttm.back.dto.CommentResponse;
import com.ttm.back.dto.CreateTaskRequest;
import com.ttm.back.dto.TaskHistoryResponse;
import com.ttm.back.dto.TaskResponse;
import com.ttm.back.dto.UpdateTaskRequest;
import com.ttm.back.dto.UserResponse;
import com.ttm.back.exception.ApiException;
import com.ttm.back.model.Project;
import com.ttm.back.model.Role;
import com.ttm.back.model.Task;
import com.ttm.back.model.TaskComment;
import com.ttm.back.model.TaskHistory;
import com.ttm.back.model.TaskPriority;
import com.ttm.back.model.TaskStatus;
import com.ttm.back.model.User;
import com.ttm.back.repository.ProjectMemberRepository;
import com.ttm.back.repository.ProjectRepository;
import com.ttm.back.repository.TaskCommentRepository;
import com.ttm.back.repository.TaskHistoryRepository;
import com.ttm.back.repository.TaskRepository;
import com.ttm.back.repository.UserRepository;
import com.ttm.back.security.CurrentUser;
import com.ttm.back.security.ProjectAccessGuard;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
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
    private final TaskCommentRepository taskCommentRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final ProjectAccessGuard accessGuard;

    public TaskService(TaskRepository taskRepository,
                        ProjectRepository projectRepository,
                        ProjectMemberRepository projectMemberRepository,
                        UserRepository userRepository,
                        TaskCommentRepository taskCommentRepository,
                        TaskHistoryRepository taskHistoryRepository,
                        ProjectAccessGuard accessGuard) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.taskHistoryRepository = taskHistoryRepository;
        this.accessGuard = accessGuard;
    }

    public List<TaskResponse> listTasks(Long projectId, TaskStatus status, TaskPriority priority,
                                         Long assignedUserId, LocalDate dueBefore, LocalDate dueAfter, String q) {
        List<Long> allowedProjectIds = null;
        if (projectId != null) {
            accessGuard.assertCanView(projectId);
        } else if (CurrentUser.getRole() != Role.admin) {
            allowedProjectIds = accessGuard.visibleProjectIds();
            if (allowedProjectIds.isEmpty()) {
                return List.of();
            }
        }

        Specification<Task> spec = buildSpecification(projectId, allowedProjectIds, status, priority, assignedUserId, dueBefore, dueAfter, q);
        List<Task> tasks = taskRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
        Map<Long, User> assigneesById = loadUsers(tasks.stream().map(Task::getAssignedUserId).filter(Objects::nonNull).distinct().toList());
        return tasks.stream().map(task -> toResponse(task, assigneesById.get(task.getAssignedUserId()))).toList();
    }

    public TaskResponse getTask(Long id) {
        Task task = findTaskOrThrow(id);
        accessGuard.assertCanView(task.getProjectId());
        UserResponse assignee = task.getAssignedUserId() != null
                ? UserResponse.fromEntity(userRepository.findById(task.getAssignedUserId()).orElse(null))
                : null;
        return TaskResponse.fromEntity(task, assignee);
    }

    public TaskResponse createTask(CreateTaskRequest request) {
        Project project = findProjectOrThrow(request.getProjectId());
        if (!project.isActive()) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot create tasks in an inactive project");
        }

        if (CurrentUser.getRole() != Role.admin
                && !projectMemberRepository.existsByProjectIdAndUserId(request.getProjectId(), CurrentUser.get())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You must be a member of this project to create tasks in it");
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

    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        CurrentUser.requireAdmin();
        Task task = findTaskOrThrow(taskId);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (task.getPriority() != request.getPriority()) {
            recordChange(taskId, "priority", task.getPriority().name(), request.getPriority().name());
        }
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        Task saved = taskRepository.save(task);
        UserResponse assignee = saved.getAssignedUserId() != null
                ? UserResponse.fromEntity(userRepository.findById(saved.getAssignedUserId()).orElse(null))
                : null;
        return TaskResponse.fromEntity(saved, assignee);
    }

    public TaskResponse updateStatus(Long taskId, TaskStatus status) {
        Task task = findTaskOrThrow(taskId);
        boolean isAssignee = Objects.equals(task.getAssignedUserId(), CurrentUser.get());
        if (CurrentUser.getRole() != Role.admin && !isAssignee) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the assignee or an admin can change this task's status");
        }

        if (task.getStatus() != status) {
            recordChange(taskId, "status", task.getStatus().name(), status.name());
        }
        task.setStatus(status);

        Task saved = taskRepository.save(task);
        UserResponse assignee = saved.getAssignedUserId() != null
                ? UserResponse.fromEntity(userRepository.findById(saved.getAssignedUserId()).orElse(null))
                : null;
        return TaskResponse.fromEntity(saved, assignee);
    }

    public TaskResponse updateAssignee(Long taskId, Long assignedUserId) {
        CurrentUser.requireAdmin();
        Task task = findTaskOrThrow(taskId);

        if (assignedUserId != null) {
            requireProjectMember(task.getProjectId(), assignedUserId);
        }

        if (!Objects.equals(task.getAssignedUserId(), assignedUserId)) {
            recordChange(taskId, "assigned_user_id",
                    task.getAssignedUserId() != null ? task.getAssignedUserId().toString() : null,
                    assignedUserId != null ? assignedUserId.toString() : null);
        }
        task.setAssignedUserId(assignedUserId);

        Task saved = taskRepository.save(task);
        UserResponse assignee = assignedUserId != null
                ? UserResponse.fromEntity(userRepository.findById(assignedUserId).orElse(null))
                : null;
        return TaskResponse.fromEntity(saved, assignee);
    }

    public void deleteTask(Long taskId) {
        CurrentUser.requireAdmin();
        Task task = findTaskOrThrow(taskId);
        taskRepository.delete(task);
    }

    public List<CommentResponse> listComments(Long taskId) {
        Task task = findTaskOrThrow(taskId);
        accessGuard.assertCanView(task.getProjectId());
        List<TaskComment> comments = taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        Map<Long, User> authorsById = loadUsers(comments.stream().map(TaskComment::getUserId).distinct().toList());
        return comments.stream()
                .map(c -> CommentResponse.fromEntity(c, UserResponse.fromEntity(authorsById.get(c.getUserId()))))
                .toList();
    }

    public CommentResponse addComment(Long taskId, String text) {
        Task task = findTaskOrThrow(taskId);
        accessGuard.assertCanView(task.getProjectId());

        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setUserId(CurrentUser.get());
        comment.setComment(text);
        TaskComment saved = taskCommentRepository.save(comment);

        User author = userRepository.findById(CurrentUser.get()).orElse(null);
        return CommentResponse.fromEntity(saved, UserResponse.fromEntity(author));
    }

    public List<TaskHistoryResponse> listHistory(Long taskId) {
        Task task = findTaskOrThrow(taskId);
        accessGuard.assertCanView(task.getProjectId());
        List<TaskHistory> history = taskHistoryRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
        Map<Long, User> usersById = loadUsers(history.stream().map(TaskHistory::getUserId).distinct().toList());
        return history.stream()
                .map(h -> TaskHistoryResponse.fromEntity(h, UserResponse.fromEntity(usersById.get(h.getUserId()))))
                .toList();
    }

    private void recordChange(Long taskId, String fieldName, String oldValue, String newValue) {
        TaskHistory history = new TaskHistory();
        history.setTaskId(taskId);
        history.setUserId(CurrentUser.get());
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        taskHistoryRepository.save(history);
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

    private Task findTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private Map<Long, User> loadUsers(List<Long> ids) {
        Map<Long, User> byId = new HashMap<>();
        userRepository.findAllById(ids).forEach(u -> byId.put(u.getId(), u));
        return byId;
    }

    private TaskResponse toResponse(Task task, User assignee) {
        return TaskResponse.fromEntity(task, assignee != null ? UserResponse.fromEntity(assignee) : null);
    }

    private Specification<Task> buildSpecification(Long projectId, List<Long> allowedProjectIds, TaskStatus status,
                                                     TaskPriority priority, Long assignedUserId,
                                                     LocalDate dueBefore, LocalDate dueAfter, String q) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (projectId != null) {
                predicates.add(cb.equal(root.get("projectId"), projectId));
            } else if (allowedProjectIds != null) {
                predicates.add(root.get("projectId").in(allowedProjectIds));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (assignedUserId != null) {
                predicates.add(cb.equal(root.get("assignedUserId"), assignedUserId));
            }
            if (dueBefore != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueBefore));
            }
            if (dueAfter != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), dueAfter));
            }
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
