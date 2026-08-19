package com.taskmaster.controllers;

import com.taskmaster.dtos.task.CreateTaskRequest;
import com.taskmaster.dtos.task.TaskResponse;
import com.taskmaster.dtos.task.UpdateTaskRequest;
import com.taskmaster.models.TaskStatus;
import com.taskmaster.services.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.create(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus status) {
        return ResponseEntity.ok(taskService.listByProject(projectId, status));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> findById(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.findById(projectId, taskId));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.update(projectId, taskId, request));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        taskService.delete(projectId, taskId);
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
    }
}
