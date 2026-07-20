package com.ttm.back.controller;

import com.ttm.back.dto.CreateTaskRequest;
import com.ttm.back.dto.TaskResponse;
import com.ttm.back.dto.UpdateAssigneeRequest;
import com.ttm.back.security.CurrentUser;
import com.ttm.back.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> list(@RequestParam Long projectId) {
        CurrentUser.requireAdmin();
        return taskService.listByProject(projectId);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        CurrentUser.requireAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @PatchMapping("/{id}/assignee")
    public TaskResponse updateAssignee(@PathVariable Long id, @RequestBody UpdateAssigneeRequest request) {
        CurrentUser.requireAdmin();
        return taskService.updateAssignee(id, request.getAssignedUserId());
    }
}
