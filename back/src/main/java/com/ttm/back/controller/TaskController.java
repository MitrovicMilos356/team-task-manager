package com.ttm.back.controller;

import com.ttm.back.dto.CommentResponse;
import com.ttm.back.dto.CreateCommentRequest;
import com.ttm.back.dto.CreateTaskRequest;
import com.ttm.back.dto.TaskHistoryResponse;
import com.ttm.back.dto.TaskResponse;
import com.ttm.back.dto.UpdateAssigneeRequest;
import com.ttm.back.dto.UpdateTaskRequest;
import com.ttm.back.dto.UpdateTaskStatusRequest;
import com.ttm.back.model.TaskPriority;
import com.ttm.back.model.TaskStatus;
import com.ttm.back.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> list(@RequestParam(required = false) Long projectId,
                                    @RequestParam(required = false) TaskStatus status,
                                    @RequestParam(required = false) TaskPriority priority,
                                    @RequestParam(required = false) Long assignedUserId,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueBefore,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueAfter,
                                    @RequestParam(required = false) String q) {
        return taskService.listTasks(projectId, status, priority, assignedUserId, dueBefore, dueAfter, q);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable Long id) {
        return taskService.getTask(id);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.updateTask(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateTaskStatusRequest request) {
        return taskService.updateStatus(id, request.getStatus());
    }

    @PatchMapping("/{id}/assignee")
    public TaskResponse updateAssignee(@PathVariable Long id, @RequestBody UpdateAssigneeRequest request) {
        return taskService.updateAssignee(id, request.getAssignedUserId());
    }

    @GetMapping("/{id}/comments")
    public List<CommentResponse> listComments(@PathVariable Long id) {
        return taskService.listComments(id);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long id, @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.addComment(id, request.getComment()));
    }

    @GetMapping("/{id}/history")
    public List<TaskHistoryResponse> history(@PathVariable Long id) {
        return taskService.listHistory(id);
    }
}
